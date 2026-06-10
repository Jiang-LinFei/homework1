"""A3T-GCN 交通速度预测: 训练 / 评估 / 可视化主程序。

数据集: METR-LA (洛杉矶都会区 207 个环形检测器, 2012.03-2012.06),
由 PyTorch Geometric Temporal 提供。模型对每个节点未来 12 个 5-min
时间步 (1 小时) 的归一化速度进行预测。

用法:
    python src/train.py                 # 默认配置
    python src/train.py --epochs 30 --max-samples 8000
"""

import argparse
import json
import os
import time

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
import torch
from torch.utils.data import DataLoader, TensorDataset

from torch_geometric_temporal.dataset import METRLADatasetLoader
from torch_geometric_temporal.signal import temporal_signal_split

from model import A3TGCNModel
from utils import mae, mape, rmse, speed_stats

RESULTS_DIR = os.path.join(os.path.dirname(__file__), "..", "results")
DATA_DIR = os.path.join(os.path.dirname(__file__), "..", "data")


def parse_args():
    p = argparse.ArgumentParser(description="A3T-GCN 交通预测")
    p.add_argument("--epochs", type=int, default=20)
    p.add_argument("--batch-size", type=int, default=32)
    p.add_argument("--lr", type=float, default=1e-2)
    p.add_argument("--periods", type=int, default=12, help="输入/输出时间步")
    p.add_argument("--max-samples", type=int, default=6000,
                   help="为 CPU 训练限制样本量, 设为 0 表示使用全部")
    p.add_argument("--seed", type=int, default=42)
    p.add_argument("--eval-only", action="store_true",
                   help="加载 results/a3tgcn_model.pt 仅评估, 不训练")
    return p.parse_args()


def load_data(periods, max_samples):
    """加载 METR-LA 并返回 (train_ds, test_ds, edge_index, edge_weight)。"""
    loader = METRLADatasetLoader(raw_data_dir=DATA_DIR)
    dataset = loader.get_dataset(num_timesteps_in=periods,
                                 num_timesteps_out=periods)
    train_set, test_set = temporal_signal_split(dataset, train_ratio=0.8)
    edge_index = torch.tensor(dataset.edge_index, dtype=torch.long)
    edge_weight = torch.tensor(dataset.edge_weight, dtype=torch.float)

    def stack(sig, cap):
        xs, ys = [], []
        for i, snap in enumerate(sig):
            if cap and i >= cap:
                break
            xs.append(snap.x.numpy())
            ys.append(snap.y.numpy())
        return (torch.from_numpy(np.stack(xs)).float(),
                torch.from_numpy(np.stack(ys)).float())

    test_cap = max_samples // 4 if max_samples else 0
    train_x, train_y = stack(train_set, max_samples)
    test_x, test_y = stack(test_set, test_cap)
    return (train_x, train_y), (test_x, test_y), edge_index, edge_weight


def evaluate(model, loader, edge_index, edge_weight):
    model.eval()
    preds, trues = [], []
    with torch.no_grad():
        for x, y in loader:
            if x.size(0) != model.tgnn.batch_size:
                continue
            out = model(x, edge_index, edge_weight)
            preds.append(out)
            trues.append(y)
    if not preds:
        raise RuntimeError(
            "评估集为空: 测试样本数小于 batch_size, 所有批次都被丢弃。"
            "请增大 --max-samples 或减小 --batch-size。")
    pred = torch.cat(preds)
    true = torch.cat(trues)
    return mae(pred, true), rmse(pred, true), pred, true


def main():
    args = parse_args()
    torch.manual_seed(args.seed)
    np.random.seed(args.seed)
    os.makedirs(RESULTS_DIR, exist_ok=True)

    print("加载 METR-LA 数据集 ...")
    (train_x, train_y), (test_x, test_y), edge_index, edge_weight = load_data(
        args.periods, args.max_samples)
    print(f"训练样本: {train_x.shape[0]}, 测试样本: {test_x.shape[0]}, "
          f"节点数: {train_x.shape[1]}, 节点特征: {train_x.shape[2]}, "
          f"时间步: {train_x.shape[3]}")

    train_loader = DataLoader(TensorDataset(train_x, train_y),
                              batch_size=args.batch_size, shuffle=True,
                              drop_last=True)
    test_loader = DataLoader(TensorDataset(test_x, test_y),
                             batch_size=args.batch_size, shuffle=False,
                             drop_last=True)

    model = A3TGCNModel(node_features=train_x.shape[2], periods=args.periods,
                        batch_size=args.batch_size)

    model_path = os.path.join(RESULTS_DIR, "a3tgcn_model.pt")
    history = []
    if args.eval_only:
        print(f"加载已训练模型: {model_path}")
        model.load_state_dict(torch.load(model_path))
    else:
        optimizer = torch.optim.Adam(model.parameters(), lr=args.lr)
        loss_fn = torch.nn.MSELoss()
        print("开始训练 ...")
        for epoch in range(1, args.epochs + 1):
            model.train()
            t0 = time.time()
            epoch_loss = 0.0
            n_batch = 0
            for x, y in train_loader:
                optimizer.zero_grad()
                out = model(x, edge_index, edge_weight)
                loss = loss_fn(out, y)
                loss.backward()
                optimizer.step()
                epoch_loss += loss.item()
                n_batch += 1
            avg = epoch_loss / max(n_batch, 1)
            history.append(avg)
            print(f"  Epoch {epoch:02d}/{args.epochs}  "
                  f"train_mse={avg:.4f}  ({time.time() - t0:.1f}s)", flush=True)

    print("评估 ...")
    test_mae, test_rmse, pred, true = evaluate(
        model, test_loader, edge_index, edge_weight)

    # 把归一化误差还原为真实速度单位 (mph)
    speed_mean, speed_std = speed_stats(
        os.path.join(DATA_DIR, "node_values.npy"))
    test_mae_mph = test_mae * speed_std
    test_rmse_mph = test_rmse * speed_std
    # 在真实速度尺度上计算 MAPE (掩掉 METR-LA 用 0 表示的缺失值)
    pred_mph = pred * speed_std + speed_mean
    true_mph = true * speed_std + speed_mean
    test_mape = mape(pred_mph, true_mph)
    print(f"测试集(归一化)  MAE={test_mae:.4f}  RMSE={test_rmse:.4f}")
    print(f"测试集(真实速度)  MAE={test_mae_mph:.3f} mph  "
          f"RMSE={test_rmse_mph:.3f} mph  MAPE={test_mape:.2f}%")

    metrics = {
        "dataset": "METR-LA (207 sensors, Los Angeles, 2012.03-2012.06)",
        "model": "A3T-GCN (Attention Temporal Graph Convolutional Network)",
        "epochs": args.epochs,
        "batch_size": args.batch_size,
        "lr": args.lr,
        "periods_in": args.periods,
        "periods_out": args.periods,
        "train_samples": int(train_x.shape[0]),
        "test_samples": int(test_x.shape[0]),
        "test_MAE_normalized": round(test_mae, 4),
        "test_RMSE_normalized": round(test_rmse, 4),
        "test_MAE_mph": round(test_mae_mph, 3),
        "test_RMSE_mph": round(test_rmse_mph, 3),
        "test_MAPE_pct": round(test_mape, 3),
        "speed_mean_mph": round(speed_mean, 3),
        "speed_std_mph": round(speed_std, 3),
        "final_train_mse": round(history[-1], 4) if history else None,
        "note": "MAE/RMSE 同时给出 z-score 归一化尺度与还原后的真实速度(mph)尺度",
    }
    with open(os.path.join(RESULTS_DIR, "metrics.json"), "w") as f:
        json.dump(metrics, f, ensure_ascii=False, indent=2)
    torch.save(model.state_dict(), os.path.join(RESULTS_DIR, "a3tgcn_model.pt"))

    # 训练损失曲线
    if history:
        plt.figure(figsize=(7, 4))
        plt.plot(range(1, len(history) + 1), history, marker="o",
                 color="#2b6cb0")
        plt.xlabel("Epoch")
        plt.ylabel("Train MSE")
        plt.title("A3T-GCN Training Loss")
        plt.grid(True, alpha=0.3)
        plt.tight_layout()
        plt.savefig(os.path.join(RESULTS_DIR, "training_loss.png"), dpi=120)
        plt.close()

    # 预测 vs 真实 (取一个节点, 预测视野第 1 步)
    node = 0
    horizon = 0
    n_show = min(288, pred.shape[0])  # 约一天 (5min * 288)
    p = pred[:n_show, node, horizon].numpy()
    t = true[:n_show, node, horizon].numpy()
    plt.figure(figsize=(10, 4))
    plt.plot(t, label="Ground truth", color="#2f855a")
    plt.plot(p, label="Prediction", color="#c53030", alpha=0.8)
    plt.xlabel("Time step (5 min interval)")
    plt.ylabel("Normalized speed")
    plt.title(f"A3T-GCN Prediction vs Ground Truth (sensor #{node})")
    plt.legend()
    plt.grid(True, alpha=0.3)
    plt.tight_layout()
    plt.savefig(os.path.join(RESULTS_DIR, "prediction_vs_truth.png"), dpi=120)
    plt.close()

    print(f"结果已保存到 {os.path.abspath(RESULTS_DIR)}")


if __name__ == "__main__":
    main()
