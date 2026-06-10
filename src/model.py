"""A3T-GCN 交通预测模型定义。

基于 PyTorch Geometric Temporal 的 A3TGCN2 单元构建,
用于在城市路网上对交通速度进行多步预测。
"""

import torch
import torch.nn.functional as F
from torch_geometric_temporal.nn.recurrent import A3TGCN2


class A3TGCNModel(torch.nn.Module):
    """注意力时空图卷积网络 (A3T-GCN)。

    输入: 每个节点过去 ``periods`` 个时间步的特征。
    输出: 每个节点未来 ``periods`` 个时间步的速度预测。
    """

    def __init__(self, node_features: int, periods: int, batch_size: int,
                 hidden_dim: int = 32):
        super().__init__()
        # A3T-GCN 单元: GCN 捕获空间依赖 + GRU 捕获时间依赖 + 注意力机制
        self.tgnn = A3TGCN2(
            in_channels=node_features,
            out_channels=hidden_dim,
            periods=periods,
            batch_size=batch_size,
        )
        # 将隐藏表示映射为多步预测
        self.linear = torch.nn.Linear(hidden_dim, periods)

    def forward(self, x: torch.Tensor, edge_index: torch.Tensor,
                edge_weight: torch.Tensor = None) -> torch.Tensor:
        """x: [B, N, F, T] -> 返回 [B, N, T]"""
        h = self.tgnn(x, edge_index, edge_weight)
        h = F.relu(h)
        h = self.linear(h)
        return h
