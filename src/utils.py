"""通用工具: 评估指标与数据装载。"""

import numpy as np
import torch


def mae(pred: torch.Tensor, true: torch.Tensor) -> float:
    """平均绝对误差 (Mean Absolute Error)。"""
    return torch.mean(torch.abs(pred - true)).item()


def rmse(pred: torch.Tensor, true: torch.Tensor) -> float:
    """均方根误差 (Root Mean Squared Error)。"""
    return torch.sqrt(torch.mean((pred - true) ** 2)).item()


def mape(pred: torch.Tensor, true: torch.Tensor, eps: float = 1.0) -> float:
    """平均绝对百分比误差 (Mean Absolute Percentage Error), 单位 %。

    需传入真实速度尺度 (mph) 的预测值与真值。METR-LA 用 0 表示缺失,
    因此仅在 ``|true| > eps`` 的有效样本上统计, 避免除零。
    """
    mask = torch.abs(true) > eps
    if mask.sum() == 0:
        return float("nan")
    return (torch.mean(torch.abs((pred[mask] - true[mask]) / true[mask]))
            .item() * 100.0)


def speed_stats(node_values_path: str):
    """从原始 node_values.npy 计算速度特征的全局均值/标准差。

    METR-LA 在加载时对每个特征做了 z-score 归一化, 这里用于把归一化误差
    还原为真实速度单位 (mph), 便于解释指标。
    """
    # 用 float64 统计: 对数百万个数值在 float32 下累加会产生明显精度误差,
    # 进而使还原到真实速度 (mph) 的指标出现约 0.5% 的偏差。
    X = np.load(node_values_path).transpose((1, 2, 0)).astype(np.float64)
    means = np.mean(X, axis=(0, 2))
    stds = np.std(X, axis=(0, 2))
    return float(means[0]), float(stds[0])


def build_batched_tensors(dataset, num_nodes: int, node_features: int,
                          periods: int):
    """把 PyG-Temporal 的逐快照数据集堆叠成 [样本数, N, F, T] 张量。"""
    feats = np.stack([snap.x.numpy() if hasattr(snap.x, "numpy") else snap.x
                      for snap in dataset], axis=0)
    targets = np.stack([snap.y.numpy() if hasattr(snap.y, "numpy") else snap.y
                        for snap in dataset], axis=0)
    return torch.from_numpy(feats).float(), torch.from_numpy(targets).float()
