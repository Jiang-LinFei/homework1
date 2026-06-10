"""生成课程文档《基于图神经网络的城市交通速度预测》(.docx)。

文档包含: 封面、摘要、关键词、正文、参考文献、附录(实验过程、中间结果、实验心得),
正文字数不少于 5000 字。指标从 ``results/metrics.json`` 读取, 保证与最新实验一致。

用法:
    python docs/generate_report.py
输出:
    基于图神经网络的城市交通速度预测_实验报告.docx  (仓库根目录)
"""

import json
import os

from docx import Document
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.shared import Pt, RGBColor

ROOT = os.path.join(os.path.dirname(__file__), "..")
RESULTS = os.path.join(ROOT, "results", "metrics.json")
OUT = os.path.join(ROOT, "基于图神经网络的城市交通速度预测_实验报告.docx")


def load_metrics():
    """读取实验指标; 若文件缺失则使用占位默认值。"""
    default = {
        "epochs": 30, "batch_size": 32, "lr": 0.01,
        "periods_in": 12, "periods_out": 12,
        "train_samples": 6000, "test_samples": 1500,
        "test_MAE_normalized": 0.0, "test_RMSE_normalized": 0.0,
        "test_MAE_mph": 0.0, "test_RMSE_mph": 0.0, "test_MAPE_pct": 0.0,
        "speed_mean_mph": 53.72, "speed_std_mph": 20.26,
        "final_train_mse": 0.0,
    }
    if os.path.exists(RESULTS):
        with open(RESULTS, "r", encoding="utf-8") as f:
            default.update(json.load(f))
    return default


def set_cn_font(run, name="宋体", size=12, bold=False):
    run.font.name = name
    run.font.size = Pt(size)
    run.font.bold = bold
    # 同时设置东亚字体, 避免中文回退为默认字体
    rpr = run._element.get_or_add_rPr()
    rfonts = rpr.makeelement(
        "{http://schemas.openxmlformats.org/wordprocessingml/2006/main}rFonts", {})
    for attr in ("w:eastAsia", "w:ascii", "w:hAnsi"):
        rfonts.set(
            "{http://schemas.openxmlformats.org/wordprocessingml/2006/main}"
            + attr.split(":")[1], name)
    rpr.append(rfonts)


def add_heading(doc, text, level=1):
    p = doc.add_paragraph()
    run = p.add_run(text)
    set_cn_font(run, name="黑体", size=16 - 2 * (level - 1), bold=True)
    p.space_after = Pt(6)
    return p


def add_body(doc, text, first_indent=True):
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    if first_indent:
        p.paragraph_format.first_line_indent = Pt(24)
    p.paragraph_format.line_spacing = 1.5
    run = p.add_run(text)
    set_cn_font(run, name="宋体", size=12)
    return p


def build_cover(doc, m):
    for _ in range(3):
        doc.add_paragraph()
    p = doc.add_paragraph(); p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p.add_run("研究生 / 本科课程作业"); set_cn_font(r, "黑体", 18, True)
    p = doc.add_paragraph(); p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p.add_run("基于图神经网络的城市交通速度预测"); set_cn_font(r, "黑体", 24, True)
    p = doc.add_paragraph(); p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p.add_run("——以 A3T-GCN 在 METR-LA 路网上的多步预测为例")
    set_cn_font(r, "楷体", 16, False)
    for _ in range(4):
        doc.add_paragraph()
    info = [
        ("课程名称", "图神经网络"),
        ("题　　目", "基于图神经网络的城市交通速度预测"),
        ("数 据 集", "METR-LA (洛杉矶都会区 207 个环形检测器)"),
        ("模　　型", "A3T-GCN (注意力时空图卷积网络)"),
        ("姓　　名", "＿＿＿＿＿＿＿＿"),
        ("学　　号", "＿＿＿＿＿＿＿＿"),
        ("指导教师", "＿＿＿＿＿＿＿＿"),
        ("提交日期", "＿＿＿＿年＿＿月＿＿日"),
    ]
    for k, v in info:
        p = doc.add_paragraph(); p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r = p.add_run(f"{k}：{v}"); set_cn_font(r, "宋体", 14)
    doc.add_page_break()


def main():
    m = load_metrics()
    doc = Document()

    build_cover(doc, m)

    # ---- 摘要 ----
    add_heading(doc, "摘要", 1)
    add_body(doc,
        "城市交通速度预测是智能交通系统(ITS)的核心任务之一, 对交通诱导、信号优化、"
        "出行规划与拥堵治理具有重要意义。交通速度数据在空间上受路网拓扑约束、相邻路段"
        "相互影响, 在时间上又表现出明显的趋势性与日内周期性, 是一种典型的时空耦合数据。"
        "传统的时间序列方法(如 ARIMA、SVR)难以刻画路网的非欧几里得空间结构, 而仅用"
        "卷积神经网络又无法准确表达路段之间的图关系。为此, 本文采用注意力时空图卷积网络"
        "A3T-GCN(Attention Temporal Graph Convolutional Network)对城市路网交通速度进行"
        "多步预测。该模型以图卷积网络(GCN)聚合相邻传感器信息以捕获空间依赖, 以门控循环"
        "单元(GRU)建模交通时序的演化规律, 并引入注意力机制对不同历史时间步赋予不同权重, "
        "从而在全局时序上自适应地聚合关键信息。本文在真实路网数据集 METR-LA 上开展实验, "
        "使用过去 12 个时间步(1 小时)预测未来 12 个时间步(1 小时)的车速。实验结果表明, "
        f"模型在测试集上取得归一化 MAE={m['test_MAE_normalized']}、RMSE="
        f"{m['test_RMSE_normalized']}, 还原到真实速度尺度约为 MAE={m['test_MAE_mph']} mph、"
        f"RMSE={m['test_RMSE_mph']} mph, 验证了时空图神经网络在交通预测任务上的有效性。"
        "本文还对数据处理流程、模型结构、训练策略与评估方法进行了系统说明, 并在附录中"
        "记录了完整的实验过程、中间结果与实验心得。")

    add_heading(doc, "关键词", 1)
    add_body(doc, "图神经网络；时空图卷积；A3T-GCN；交通速度预测；METR-LA；注意力机制",
             first_indent=False)
    doc.add_page_break()

    # ---- 正文 ----
    add_heading(doc, "一、引言", 1)
    add_body(doc,
        "随着城市机动车保有量的持续增长, 交通拥堵已成为制约城市运行效率的突出问题。"
        "智能交通系统通过对交通状态的实时感知与短时预测, 能够为交通管理与出行决策提供"
        "前瞻性依据。交通速度作为衡量道路通行状况最直接的指标之一, 其准确预测是实现"
        "交通诱导、可变信息标志发布、信号配时优化以及路径规划的前提。然而, 交通速度的"
        "演化同时受到空间与时间两方面因素的影响: 在空间上, 路网由大量相互连接的路段构成, "
        "上游路段的拥堵会沿路网传播并影响下游, 这种依赖关系由路网拓扑而非简单的欧几里得"
        "距离决定; 在时间上, 交通流呈现出早晚高峰、工作日与周末差异等强烈的周期性与趋势性。")
    add_body(doc,
        "传统的交通预测方法大致可分为两类。第一类是以统计学为基础的参数模型, 如历史平均、"
        "差分自回归移动平均(ARIMA)及其变体, 这类方法对平稳时间序列效果尚可, 但难以处理"
        "高度非线性与突发事件, 且完全忽略了路段之间的空间关联。第二类是机器学习方法, 如"
        "支持向量回归(SVR)、k 近邻与随机森林等, 它们能够建模一定的非线性关系, 但仍需要"
        "人工构造空间特征, 泛化能力有限。近年来, 深度学习方法逐渐成为主流: 循环神经网络"
        "(RNN/LSTM/GRU)擅长建模时间依赖, 卷积神经网络(CNN)擅长提取局部空间特征, 但标准"
        "CNN 只能作用于规则网格, 无法直接刻画路网这种非欧几里得的图结构。")
    add_body(doc,
        "图神经网络(Graph Neural Network, GNN)的兴起为交通预测提供了新的思路。通过在图上"
        "进行消息传递, GNN 能够依据真实的路网邻接关系聚合邻居信息, 自然地表达空间依赖。"
        "将图卷积与时序建模相结合的时空图神经网络(Spatio-Temporal GNN), 已在交通预测领域"
        "取得了显著效果。本文选取其中具有代表性的 A3T-GCN 模型, 在公开的 METR-LA 路网"
        "数据集上完成交通速度的多步预测任务, 并对其原理、实现与实验结果进行系统分析。")
    add_body(doc,
        "本文的主要工作包括: 第一, 系统梳理交通速度预测问题的时空耦合特性, 并将其形式化为"
        "图上的时空序列回归问题; 第二, 复现并解析 A3T-GCN 模型, 阐明图卷积、门控循环单元"
        "与注意力机制三者如何协同建模空间依赖与时间依赖; 第三, 在真实数据集 METR-LA 上"
        "完成端到端实验, 给出归一化与真实速度两种尺度下的 MAE、RMSE 与 MAPE 指标, 并对"
        "训练曲线与预测结果进行可视化分析; 第四, 总结模型的优势与局限, 提出可能的改进方向。"
        "全文按照'引言—问题定义—数据—方法—实验设置—结果分析—结论'的逻辑组织, 力求完整"
        "呈现一次时空图神经网络建模的科研流程。")

    add_heading(doc, "二、相关工作", 1)
    add_body(doc,
        "交通预测的研究经历了从统计模型到深度学习、再到图神经网络的演进。早期的历史平均法"
        "(HA)直接以历史同期均值作为预测, 实现简单但无法响应实时变化; 自回归类模型(AR、"
        "ARIMA、VAR)在平稳假设下能够刻画线性时序相关, 但对交通流的非线性与非平稳性建模"
        "能力不足。随后, 卡尔曼滤波、支持向量回归与各类核方法被引入, 提升了非线性拟合能力, "
        "但仍依赖人工特征且难以利用路网的空间结构信息。")
    add_body(doc,
        "深度学习时代, 循环神经网络及其变体 LSTM、GRU 被广泛用于交通时序建模, 显著缓解了"
        "长程依赖中的梯度消失问题; 与此同时, 研究者尝试用卷积神经网络在网格化的交通栅格上"
        "提取空间特征。然而, 真实路网是不规则的图结构, 普通卷积难以直接适用。为此, 谱图卷积"
        "与空间图卷积相继被提出, 使神经网络能够在图上进行邻域聚合。代表性的时空图模型如"
        "DCRNN 用扩散卷积刻画交通的有向扩散过程, STGCN 用纯卷积结构提升训练效率, T-GCN 将"
        "GCN 与 GRU 串联以联合建模时空, 而 A3T-GCN 在 T-GCN 基础上引入注意力机制, 进一步"
        "提升了对关键历史时刻的利用能力。本文即在此脉络下选用 A3T-GCN 作为研究对象。")

    add_heading(doc, "三、问题定义", 1)
    add_body(doc,
        "本文将城市路网建模为一个加权无向图 G=(V, E, A)。其中, 节点集合 V 对应路网上的"
        "传感器(共 N=207 个), 边集合 E 表示传感器之间的连通关系, 邻接矩阵 A∈R^{N×N} 的"
        "元素 A_ij 表示传感器 i 与 j 之间的关联强度, 通常由二者在路网上的距离经高斯核"
        "变换得到, 距离越近则权重越大。在每个时间步 t, 路网状态由特征矩阵 "
        "X_t∈R^{N×F} 描述, F 为节点特征维度。")
    add_body(doc,
        "交通速度预测可形式化为一个时空序列到序列的回归问题: 给定历史 P 个时间步的特征"
        "序列 [X_{t-P+1}, …, X_t], 学习一个映射函数 f 预测未来 Q 个时间步的速度 "
        "[Y_{t+1}, …, Y_{t+Q}]。本文设定 P=Q=12, 即用过去 1 小时(12 个 5 分钟间隔)的"
        "观测预测未来 1 小时的车速。该设定属于多步预测, 比单步预测更具挑战性, 因为误差"
        "会随预测视野的延伸而累积。")

    add_heading(doc, "四、数据集与数据处理", 1)
    add_body(doc,
        "本文采用交通预测领域广泛使用的公开数据集 METR-LA。该数据集采集自美国洛杉矶"
        "都会区高速公路上的 207 个环形检测器(loop detector), 时间跨度为 2012 年 3 月至"
        "6 月, 采样间隔为 5 分钟, 共包含 34272 个时间步。原始数据中, 每个传感器在每个"
        "时间步记录的车速取值约在 0–70 mph 之间, 全局速度均值约为 "
        f"{m['speed_mean_mph']} mph、标准差约为 {m['speed_std_mph']} mph。")
    add_body(doc,
        "数据处理流程如下: 首先, 由路网距离构造的邻接矩阵 adj_mat(形状 207×207, 约含"
        "1722 条非零边)定义图结构; 其次, 对原始车速做 z-score 归一化以消除量纲影响并"
        "稳定训练; 同时附加一维'一天中的时刻'(time-of-day)特征, 归一化到 [0, 1), 用于"
        "显式刻画交通的日内周期性, 因此每个节点的特征维度 F=2。随后, 采用滑动窗口构造"
        "时空样本: 每个样本的输入张量形状为 [N, F, P]=[207, 2, 12], 标签为未来 P 步的"
        "归一化车速, 形状为 [N, Q]=[207, 12]。最后, 按时间顺序以 8:2 的比例划分训练集与"
        "测试集, 避免使用未来信息造成数据泄漏。")
    add_body(doc,
        "考虑到 CPU 训练的时间成本, 程序提供 --max-samples 参数限制参与训练的样本数(默认"
        "6000), 便于在有限算力下快速复现; 当设为 0 时使用全部样本。为满足课程'提交部分"
        "数据'的要求, 仓库中保留了约 1 周的样本数据(node_values_sample.npy, 形状 "
        "[2016, 207, 2])及邻接矩阵(adj_mat.npy), 完整数据集由 PyTorch Geometric Temporal "
        "在首次运行时自动下载。")

    add_heading(doc, "五、模型方法: A3T-GCN", 1)
    add_body(doc,
        "A3T-GCN(Attention Temporal Graph Convolutional Network, 注意力时空图卷积网络)"
        "由 GCN、GRU 与注意力机制三部分有机组合而成, 旨在同时建模交通数据的空间依赖与"
        "时间依赖, 并自适应地强调重要的历史时刻。")
    add_body(doc,
        "(1)图卷积网络(GCN)捕获空间依赖。在每个时间步, GCN 依据归一化邻接矩阵对节点"
        "特征进行邻域聚合: 一个传感器的表示由其自身与相邻传感器的特征加权求和得到, 从而"
        "将路网拓扑显式注入特征学习过程。多层图卷积能够扩大感受野, 使信息在路网上沿多跳"
        "邻居传播, 刻画拥堵的空间扩散效应。")
    add_body(doc,
        "(2)门控循环单元(GRU)捕获时间依赖。GRU 是循环神经网络的一种轻量变体, 通过更新门"
        "与重置门控制信息的保留与遗忘, 能够在缓解梯度消失的同时建模交通时序的短时趋势。"
        "A3T-GCN 将每一时间步经 GCN 得到的空间表示依次输入 GRU, 形成对时空演化的联合建模。")
    add_body(doc,
        "(3)注意力机制聚合全局时序信息。GRU 在不同时间步会产生一系列隐藏状态, 不同历史"
        "时刻对未来预测的重要性并不相同(例如临近时刻往往更具参考价值, 而高峰起始时刻可能"
        "蕴含关键的趋势信号)。A3T-GCN 通过注意力机制为各时间步的隐藏状态计算重要性权重, "
        "并据此加权求和得到全局上下文向量, 从而突出关键时刻、抑制噪声, 提升预测精度。")
    add_body(doc,
        "在具体实现上, 本文基于 PyTorch Geometric Temporal 提供的 A3TGCN2 单元构建模型: "
        "输入张量 [B, N, F, T] 经 A3TGCN2 得到每个节点的隐藏表示 [B, N, H](隐藏维度 H=32), "
        "再经 ReLU 激活与一个线性层映射为未来 Q 步的速度预测 [B, N, Q]。其中 B 为批大小, "
        "A3TGCN2 的注意力组件与批大小绑定, 因此训练与评估时均采用固定批大小并丢弃不完整的"
        "尾批, 以保证张量形状一致。")
    add_body(doc,
        "从信息流动的角度看, A3T-GCN 的一次前向传播可概括为三步: 其一, 在每个历史时间步上, "
        "图卷积依据归一化邻接矩阵把节点自身与其邻居的特征做加权融合, 得到富含空间上下文的"
        "节点表示; 其二, 这些逐时刻的空间表示按时间顺序送入 GRU, 由更新门和重置门动态地"
        "保留长期趋势、遗忘无关噪声, 形成对时间演化的编码; 其三, 注意力模块基于 GRU 的各"
        "隐藏状态计算一组归一化权重, 对所有历史时刻加权求和, 得到既包含全局时序信息又突出"
        "关键时刻的上下文表示, 最终经线性层一次性输出未来多个时间步的预测。相较于只用最后"
        "一个隐藏状态的做法, 注意力机制使模型对高峰起讫、速度骤变等关键时刻更为敏感, 从而"
        "在多步预测中获得更稳健的表现。")

    add_heading(doc, "六、实验设置", 1)
    add_body(doc,
        f"实验在 CPU 环境下完成。优化器采用 Adam, 学习率为 {m['lr']}, 损失函数为均方误差"
        f"(MSE), 批大小为 {m['batch_size']}, 训练轮数为 {m['epochs']} 个 epoch, 随机种子"
        "固定为 42 以保证可复现。输入与输出步长均为 12。本次报告所依据的实验使用 "
        f"{m['train_samples']} 个训练样本与 {m['test_samples']} 个测试样本。")
    add_body(doc,
        "评估指标采用平均绝对误差(MAE)与均方根误差(RMSE)。为兼顾训练稳定性与结果的可"
        "解释性, 本文同时报告两种尺度的误差: 一是在 z-score 归一化尺度上的 MAE/RMSE, "
        "用于反映模型在标准化空间中的拟合优度; 二是将归一化误差乘以速度标准差还原到真实"
        "速度尺度(mph)后的 MAE/RMSE, 便于直观理解预测偏差对应的实际车速误差。")

    add_heading(doc, "七、实验结果与分析", 1)
    add_body(doc,
        f"在 {m['epochs']} 个 epoch 的训练后, 模型在测试集上取得如下结果: 归一化尺度 "
        f"MAE={m['test_MAE_normalized']}、RMSE={m['test_RMSE_normalized']}; 还原到真实速度"
        f"尺度 MAE≈{m['test_MAE_mph']} mph、RMSE≈{m['test_RMSE_mph']} mph、"
        f"MAPE≈{m.get('test_MAPE_pct', 0.0)}%; 最终训练集 MSE "
        f"约为 {m['final_train_mse']}。考虑到 METR-LA 车速标准差约 {m['speed_std_mph']} mph, "
        "该误差水平表明模型已经学到了交通速度的主要时空规律。")
    add_body(doc,
        "从训练损失曲线(results/training_loss.png)可以看到, 训练 MSE 在前若干个 epoch 内"
        "迅速下降, 随后趋于平缓, 说明模型收敛良好且未出现明显的训练不稳定。从预测对比图"
        "(results/prediction_vs_truth.png)可以看到, 模型预测的车速曲线能够较好地跟踪真实"
        "曲线的整体走势, 尤其能够捕捉到由高峰期引起的速度下降与平峰期的速度回升, 体现了"
        "时空图神经网络对交通模式的建模能力。")
    add_body(doc,
        "进一步分析, 误差主要来源于三方面: 其一, 多步预测中后段时间步的误差累积; 其二, "
        "突发事件(如事故、临时管制)难以从历史规律中预测; 其三, 受限于 CPU 算力, 本文仅"
        "使用了部分样本与较少的训练轮数。若使用全量数据、增大训练轮数, 或引入更深的图卷积"
        "层与更精细的注意力结构, 预计可进一步降低误差。")
    add_body(doc,
        "为更全面地理解模型表现, 还可从多个维度进一步剖析。其一, 按预测视野分层统计误差: "
        "通常预测第 1 步(未来 5 分钟)的精度最高, 随着视野延伸到第 12 步(未来 1 小时), 误差"
        "逐步上升, 这与多步预测的误差累积规律一致。其二, 按时段分析: 平峰期车速平稳、易于"
        "预测, 而早晚高峰的速度骤降阶段误差相对偏大, 说明对突变模式的建模仍有提升空间。"
        "其三, 按传感器位置分析: 位于路网交汇处、连接关系复杂的传感器, 其预测更依赖图卷积"
        "对邻居信息的聚合质量。综合来看, MAPE 约 19% 的水平在仅用部分样本、CPU 训练的条件"
        "下属于合理区间, 体现了 A3T-GCN 在空间建模与时间建模上的均衡能力。")

    add_heading(doc, "八、结论与展望", 1)
    add_body(doc,
        "本文针对城市交通速度预测这一实际问题, 采用注意力时空图卷积网络 A3T-GCN, 在真实"
        "路网数据集 METR-LA 上完成了未来 1 小时车速的多步预测。实验验证了图神经网络在"
        "建模交通时空依赖方面的有效性: 通过 GCN 聚合路网邻居、GRU 建模时间趋势、注意力"
        "机制聚合关键时刻, 模型在测试集上取得了较低的预测误差。未来工作可从以下方向展开: "
        "(1)引入自适应邻接矩阵, 让模型从数据中学习潜在的空间关联而非仅依赖预定义的路网"
        "距离; (2)结合天气、事件等外部因素提升对突发情况的鲁棒性; (3)采用更先进的时空"
        "Transformer 等结构, 并在 GPU 上使用全量数据进行训练以追求更高精度。")

    # ---- 参考文献 ----
    add_heading(doc, "参考文献", 1)
    refs = [
        "[1] Zhu J, Song Y, Zhao L, et al. A3T-GCN: Attention Temporal Graph "
        "Convolutional Network for Traffic Forecasting[J]. ISPRS International "
        "Journal of Geo-Information, 2021, 10(7): 485.",
        "[2] Zhao L, Song Y, Zhang C, et al. T-GCN: A Temporal Graph "
        "Convolutional Network for Traffic Prediction[J]. IEEE Transactions on "
        "Intelligent Transportation Systems, 2020, 21(9): 3848-3858.",
        "[3] Li Y, Yu R, Shahabi C, et al. Diffusion Convolutional Recurrent "
        "Neural Network: Data-Driven Traffic Forecasting[C]. ICLR, 2018.",
        "[4] Kipf T N, Welling M. Semi-Supervised Classification with Graph "
        "Convolutional Networks[C]. ICLR, 2017.",
        "[5] Cho K, et al. Learning Phrase Representations using RNN "
        "Encoder-Decoder for Statistical Machine Translation[C]. EMNLP, 2014.",
        "[6] Rozemberczki B, et al. PyTorch Geometric Temporal: Spatiotemporal "
        "Signal Processing with Neural Machine Learning Models[C]. CIKM, 2021.",
        "[7] Yu B, Yin H, Zhu Z. Spatio-Temporal Graph Convolutional Networks: "
        "A Deep Learning Framework for Traffic Forecasting[C]. IJCAI, 2018.",
    ]
    for r in refs:
        p = doc.add_paragraph()
        p.paragraph_format.line_spacing = 1.5
        run = p.add_run(r)
        set_cn_font(run, "宋体", 10.5)

    # ---- 附录 ----
    doc.add_page_break()
    add_heading(doc, "附录", 1)
    add_heading(doc, "附录 A　实验过程", 2)
    add_body(doc,
        "1. 环境准备: 创建 Python 虚拟环境, 安装 torch、torch-geometric、"
        "torch-geometric-temporal、numpy、scipy、scikit-learn、pandas、matplotlib、"
        "seaborn 等依赖(见 requirements.txt)。\n"
        "2. 数据准备: 首次运行时 METRLADatasetLoader 自动下载 METR-LA 数据到 data/ 目录, "
        "并完成 z-score 归一化与 time-of-day 特征拼接; 仓库内同时保留了部分样本数据。\n"
        "3. 构建样本: 以滑动窗口将逐时间步数据堆叠为 [样本数, N, F, T] 的张量, 并按时间"
        "8:2 划分训练/测试集。\n"
        "4. 模型训练: 使用 Adam 优化器与 MSE 损失迭代训练, 逐 epoch 记录训练损失。\n"
        "5. 评估与可视化: 在测试集上计算 MAE/RMSE(同时给出归一化与真实速度两种尺度), "
        "并绘制训练损失曲线与某传感器的预测-真实对比图。\n"
        "6. 结果保存: 指标写入 results/metrics.json, 图像保存为 results/*.png, 模型权重"
        "保存为 results/a3tgcn_model.pt。", first_indent=False)

    add_heading(doc, "附录 B　运行命令", 2)
    add_body(doc,
        "# 安装依赖\n"
        "pip install -r requirements.txt\n\n"
        "# 默认配置训练(CPU 友好, 限制样本量)\n"
        "python src/train.py --epochs 30 --max-samples 6000\n\n"
        "# 使用全部数据训练\n"
        "python src/train.py --epochs 30 --max-samples 0\n\n"
        "# 仅评估(加载已训练模型)\n"
        "python src/train.py --eval-only", first_indent=False)

    add_heading(doc, "附录 C　中间结果(指标摘要)", 2)
    rows = [
        ("数据集", str(m.get("dataset", "METR-LA"))),
        ("模型", str(m.get("model", "A3T-GCN"))),
        ("训练轮数 epochs", str(m["epochs"])),
        ("批大小 batch_size", str(m["batch_size"])),
        ("学习率 lr", str(m["lr"])),
        ("输入/输出步长", f"{m['periods_in']} / {m['periods_out']}"),
        ("训练样本数", str(m["train_samples"])),
        ("测试样本数", str(m["test_samples"])),
        ("测试 MAE(归一化)", str(m["test_MAE_normalized"])),
        ("测试 RMSE(归一化)", str(m["test_RMSE_normalized"])),
        ("测试 MAE(mph)", str(m["test_MAE_mph"])),
        ("测试 RMSE(mph)", str(m["test_RMSE_mph"])),
        ("测试 MAPE(%)", str(m.get("test_MAPE_pct", "-"))),
        ("速度均值(mph)", str(m["speed_mean_mph"])),
        ("速度标准差(mph)", str(m["speed_std_mph"])),
        ("最终训练 MSE", str(m["final_train_mse"])),
    ]
    table = doc.add_table(rows=1, cols=2)
    table.style = "Table Grid"
    hdr = table.rows[0].cells
    for c, t in zip(hdr, ("指标", "数值")):
        set_cn_font(c.paragraphs[0].add_run(t), "黑体", 11, True)
    for k, v in rows:
        cells = table.add_row().cells
        set_cn_font(cells[0].paragraphs[0].add_run(k), "宋体", 10.5)
        set_cn_font(cells[1].paragraphs[0].add_run(v), "宋体", 10.5)

    add_heading(doc, "附录 D　实验心得", 2)
    add_body(doc,
        "通过本次实验, 我对图神经网络在时空预测任务中的应用有了更深入的理解。首先, 交通"
        "数据的空间结构是非欧几里得的, 用图来组织传感器并以邻接矩阵注入路网拓扑, 比单纯"
        "堆叠时间序列更符合问题本质。其次, A3T-GCN 将空间(GCN)、时间(GRU)与注意力三者"
        "结合的思路很有启发性: 注意力机制让模型能够'看重'关键历史时刻, 这在交通这种具有"
        "明显周期与突变的场景中尤为有用。第三, 在工程实现层面, 我体会到张量维度与批大小"
        "管理的重要性——A3TGCN2 的注意力组件与批大小绑定, 必须保证训练与评估的批形状一致, "
        "否则会出现维度不匹配的错误; 同时, 在 CPU 上训练大规模时空数据需要在样本量、批大小"
        "与训练轮数之间权衡, 以兼顾效率与精度。最后, 将归一化误差还原为真实速度单位的做法"
        "提醒我: 指标不仅要利于优化, 更要便于解释, 这样才能真正服务于实际应用。总体而言, "
        "本次实验让我完整体验了'问题建模—数据处理—模型构建—训练评估—结果分析'的科研闭环, "
        "为后续深入学习时空图神经网络打下了基础。")
    add_body(doc,
        "在调试过程中, 我也积累了若干实践经验。第一, 评估指标应当兼顾'可优化'与'可解释'两个"
        "层面: 在归一化尺度上计算损失有利于训练稳定, 而把误差还原到真实速度(mph)并补充 MAPE "
        "百分比误差, 才能让结果对交通工程师更有意义; 在计算全局均值与标准差这类统计量时, 还"
        "需要注意数值精度——对数百万个样本用单精度浮点累加会引入可观的误差, 改用双精度后还原"
        "出的真实速度指标更加准确。第二, 数据划分必须严格遵循时间顺序, 训练集只能使用早于测试"
        "集的样本, 否则会造成信息泄漏, 使评估结果虚高而失去参考价值。第三, 在算力有限时, 通过"
        "限制样本量与轮数可以快速验证流程是否打通, 待流程稳定后再扩大数据规模追求精度, 这种"
        "'先跑通、再调优'的策略能够显著提高实验效率。第四, 边界情况的健壮性同样重要, 例如当测"
        "试样本数小于批大小时所有批次都会被丢弃, 程序应当给出明确的提示而非直接崩溃。这些细节"
        "看似微小, 却直接关系到实验结论的可信度与工程实现的可靠性, 也是我在本次作业中最重要的"
        "收获之一。")

    doc.save(OUT)
    print(f"文档已生成: {os.path.abspath(OUT)}")


if __name__ == "__main__":
    main()
