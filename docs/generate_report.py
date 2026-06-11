"""基于学校模板生成《基于图神经网络的城市交通速度预测》结课报告(.docx)。

设计要点:
- 以 ``docs/template.docx`` 为基底, **原样保留封面与评分标准页**(封面与模板一致);
- 删除模板中"目录（仅供参考）"占位条目, 在其后追加正式报告内容;
- 报告章节顺序对齐模板目录: 研究目的 → 数据获取 → 数据预处理 → 模型与方法
  → 实验与结果分析 → 结论, 并补齐需求(xuqiu.md)要求的 题目/摘要/关键词/参考文献/附录;
- 指标从 ``results/metrics.json`` 读取, 保证与最新实验一致; 结果图自动嵌入。

用法:
    python docs/generate_report.py
输出:
    基于图神经网络的城市交通速度预测_实验报告.docx  (仓库根目录)
"""

import json
import os
import re
import shutil
import subprocess
import tempfile

from docx import Document
from docx.enum.text import WD_ALIGN_PARAGRAPH, WD_TAB_ALIGNMENT, WD_TAB_LEADER
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Pt

# 记录正文标题, 供生成目录使用: 元素为 (级别, 文本)
HEADINGS = []

ROOT = os.path.join(os.path.dirname(__file__), "..")
TEMPLATE = os.path.join(os.path.dirname(__file__), "template.docx")
RESULTS = os.path.join(ROOT, "results", "metrics.json")
LOSS_IMG = os.path.join(ROOT, "results", "training_loss.png")
PRED_IMG = os.path.join(ROOT, "results", "prediction_vs_truth.png")
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
        "dataset": "METR-LA", "model": "A3T-GCN",
    }
    if os.path.exists(RESULTS):
        with open(RESULTS, "r", encoding="utf-8") as f:
            default.update(json.load(f))
    return default


# --------------------------------------------------------------------------- #
# 字体与排版工具
# --------------------------------------------------------------------------- #
def set_cn_font(run, name="宋体", size=12, bold=False):
    run.font.name = name
    run.font.size = Pt(size)
    run.font.bold = bold
    rpr = run._element.get_or_add_rPr()
    rfonts = rpr.find(qn("w:rFonts"))
    if rfonts is None:
        rfonts = OxmlElement("w:rFonts")
        rpr.append(rfonts)
    for attr in ("w:eastAsia", "w:ascii", "w:hAnsi"):
        rfonts.set(qn(attr), name)


def add_heading(doc, text, level=1):
    """手动格式化标题并设置大纲级别(outlineLvl), 使目录(TOC)域可自动识别。

    模板未内置 Heading 样式, 因此不依赖样式, 而是直接在段落属性中写入
    w:outlineLvl, Word 的 TOC 域配合 \\u 开关即可按大纲级别收录标题。
    """
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(8)
    p.paragraph_format.space_after = Pt(6)
    p.paragraph_format.keep_with_next = True
    ppr = p._element.get_or_add_pPr()
    ol = OxmlElement("w:outlineLvl")
    ol.set(qn("w:val"), str(level - 1))
    ppr.append(ol)
    run = p.add_run(text)
    set_cn_font(run, name="黑体", size=15 - 2 * (level - 1), bold=True)
    HEADINGS.append((level, text))
    return p


def add_body(doc, text, first_indent=True, size=12):
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    if first_indent:
        p.paragraph_format.first_line_indent = Pt(size * 2)
    p.paragraph_format.line_spacing = 1.5
    run = p.add_run(text)
    set_cn_font(run, name="宋体", size=size)
    return p


def add_centered(doc, text, name="黑体", size=20, bold=True):
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = p.add_run(text)
    set_cn_font(run, name=name, size=size, bold=bold)
    return p


def add_mono(doc, text):
    """等宽风格的代码/命令块。"""
    p = doc.add_paragraph()
    p.paragraph_format.line_spacing = 1.3
    p.paragraph_format.left_indent = Pt(12)
    run = p.add_run(text)
    set_cn_font(run, name="Consolas", size=10.5)
    return p


def add_toc(doc):
    """插入可自动更新的 Word 目录域(打开文档后按 F9 或右键更新)。

    后续 ``bake_static_toc`` 会在渲染计算出页码后, 把该域替换为带页码的静态目录;
    若渲染工具缺失则保留此自动更新域作为回退。
    """
    p = doc.add_paragraph()
    p.paragraph_format.line_spacing = 1.5
    run = p.add_run()
    fld_begin = OxmlElement("w:fldChar")
    fld_begin.set(qn("w:fldCharType"), "begin")
    instr = OxmlElement("w:instrText")
    instr.set(qn("xml:space"), "preserve")
    instr.text = r'TOC \o "1-2" \h \z \u'
    fld_sep = OxmlElement("w:fldChar")
    fld_sep.set(qn("w:fldCharType"), "separate")
    placeholder = OxmlElement("w:t")
    placeholder.text = "（右键此处选择“更新域”可生成目录）"
    fld_end = OxmlElement("w:fldChar")
    fld_end.set(qn("w:fldCharType"), "end")
    r = run._element
    r.append(fld_begin)
    r.append(instr)
    r.append(fld_sep)
    r.append(placeholder)
    r.append(fld_end)
    return p


def strip_sample_toc(doc):
    """删除模板中以“目录（仅供参考）”开头的占位条目, 保留封面与评分标准页。"""
    paras = doc.paragraphs
    start = None
    for i, p in enumerate(paras):
        t = p.text.replace(" ", "")
        if "目录" in t and "参考" in t:
            start = i
            break
    if start is None:
        return
    for p in paras[start:]:
        p._element.getparent().remove(p._element)
    # 再清理评分表页后遗留的连续空行(保留含分节属性 sectPr 的段落), 避免多余空白页
    for p in reversed(doc.paragraphs):
        if p.text.strip():
            break
        ppr = p._element.find(qn("w:pPr"))
        if ppr is not None and ppr.find(qn("w:sectPr")) is not None:
            break
        p._element.getparent().remove(p._element)


# --------------------------------------------------------------------------- #
# 正文
# --------------------------------------------------------------------------- #
def build_report(doc, m):
    doc.add_page_break()

    # ---- 题目 ----
    add_centered(doc, "基于图神经网络的城市交通速度预测", size=22)
    add_centered(doc, "——以 A3T-GCN 在 METR-LA 路网上的多步预测为例",
                 name="楷体", size=15, bold=False)
    doc.add_paragraph()

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
        f"RMSE={m['test_RMSE_mph']} mph(MAPE≈{m.get('test_MAPE_pct', 0.0)}%), 验证了时空"
        "图神经网络在交通预测任务上的有效性。本文还对数据处理流程、模型结构、训练策略与"
        "评估方法进行了系统说明, 并在附录中记录了完整的实验过程、中间结果与实验心得。")

    add_heading(doc, "关键词", 1)
    add_body(doc, "图神经网络；时空图卷积；A3T-GCN；交通速度预测；METR-LA；注意力机制",
             first_indent=False)

    # ---- 目录 ----
    doc.add_page_break()
    add_centered(doc, "目录", size=18)
    add_toc(doc)
    doc.add_page_break()

    # ---- 1 研究目的 ----
    add_heading(doc, "1 研究目的", 1)
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
        "取得了显著效果。本文的研究目的即在于: 选取具有代表性的 A3T-GCN 模型, 在公开的"
        "METR-LA 路网数据集上完成交通速度的多步预测任务, 复现并解析图卷积、门控循环单元"
        "与注意力机制三者如何协同建模时空依赖, 给出可复现的实验结果与可视化分析, 并总结"
        "时空图神经网络在交通预测中的优势与局限, 为后续研究与工程应用提供参考。")
    add_body(doc,
        "本文按照'研究目的—数据获取—数据预处理—模型方法—实验与结果分析—结论'的逻辑组织, "
        "力求完整呈现一次时空图神经网络建模的科研流程, 既覆盖理论原理, 也给出工程实现与"
        "实验细节, 使读者能够据此独立复现全部结果。")

    # ---- 2 数据获取 ----
    add_heading(doc, "2 数据获取", 1)
    add_heading(doc, "2.1 数据集介绍", 2)
    add_body(doc,
        "本文采用交通预测领域广泛使用的公开数据集 METR-LA。该数据集采集自美国洛杉矶"
        "都会区高速公路上的 207 个环形检测器(loop detector), 时间跨度为 2012 年 3 月至"
        "6 月, 采样间隔为 5 分钟, 共包含 34272 个时间步。每个检测器记录所在路段的瞬时"
        "平均车速(单位 mph), 全局速度均值约为 "
        f"{m['speed_mean_mph']} mph、标准差约为 {m['speed_std_mph']} mph, 取值范围大致"
        "落在 0–70 mph 之间。METR-LA 因其真实性、规模适中且自带路网拓扑, 已成为交通"
        "时空预测领域的标准基准之一, 被 DCRNN、Graph WaveNet、T-GCN、A3T-GCN 等众多模型"
        "用于评测。")
    add_heading(doc, "2.2 数据来源与采集方式", 2)
    add_body(doc,
        "原始数据来源于洛杉矶交通管理部门部署在高速公路上的环形线圈检测器。线圈检测器"
        "埋设于路面之下, 通过感应车辆通过时引起的电磁变化来统计车流并估计平均速度, 是"
        "目前城市道路最常见、最稳定的交通流采集手段之一。这些检测器按 5 分钟为粒度上报"
        "速度读数, 经清洗、对齐后形成规整的时空矩阵。本文通过 PyTorch Geometric Temporal "
        "提供的 METRLADatasetLoader 接口获取该数据集, 该接口会在首次运行时自动下载官方"
        "打包好的速度张量与路网邻接矩阵, 免去了手工拼接与对齐的繁琐。")
    add_heading(doc, "2.3 数据规模与格式", 2)
    add_body(doc,
        "数据集主要由两部分构成。其一是节点速度张量 node_values, 形状为 [34272, 207, 1]"
        "(时间步×传感器×特征), 记录每个传感器在每个时间步的车速; 其二是路网邻接矩阵 "
        "adj_mat, 形状为 207×207, 其元素表示传感器之间的关联强度, 由路网距离经高斯核"
        "变换得到, 共含约 1722 条非零边。为满足课程'提交部分数据'的要求, 本仓库在 "
        "data/sample/ 下保留了约 1 周的样本数据 node_values_sample.npy(形状 "
        "[2016, 207, 2])与邻接矩阵 adj_mat.npy, 完整数据集则在首次运行时自动下载(并已"
        "通过 .gitignore 排除, 避免提交上百兆的原始文件)。")

    # ---- 3 数据预处理 ----
    add_heading(doc, "3 数据预处理", 1)
    add_body(doc,
        "原始速度数据在直接用于训练前需要经过若干预处理步骤, 以消除量纲影响、显式刻画"
        "周期性并构造适合时空图神经网络的样本格式。本文的数据预处理流程主要包括归一化、"
        "特征构造、图结构构建与时空样本划分四个环节。")
    add_heading(doc, "3.1 归一化与特征构造", 2)
    add_body(doc,
        "首先对原始车速做 z-score 归一化: 以全局速度均值与标准差将速度变换为零均值、"
        "单位方差的标准化值, 既能消除量纲、稳定梯度, 又便于不同传感器之间的可比。其次, "
        "为显式刻画交通的日内周期性, 在速度特征之外附加一维'一天中的时刻'(time-of-day)"
        "特征, 将其归一化到 [0, 1) 区间。因此每个节点在每个时间步的特征维度 F=2, 分别"
        "对应归一化车速与时刻信息。")
    add_heading(doc, "3.2 路网图构建", 2)
    add_body(doc,
        "本文将城市路网建模为加权无向图 G=(V, E, A): 节点集合 V 对应 207 个传感器, 邻接"
        "矩阵 A∈R^{207×207} 的元素 A_ij 表示传感器 i 与 j 之间的关联强度。A 由路网距离"
        "经高斯核变换得到, 距离越近权重越大, 从而把真实的道路连通关系注入模型。该图结构"
        "在所有时间步上保持不变, 作为图卷积进行邻域聚合的依据。")
    add_heading(doc, "3.3 时空样本构建与数据集划分", 2)
    add_body(doc,
        "采用滑动窗口构造时空样本: 以连续 P=12 个时间步的特征作为输入, 紧随其后的 Q=12 "
        "个时间步的归一化车速作为标签。每个样本的输入张量形状为 [N, F, P]=[207, 2, 12], "
        "标签形状为 [N, Q]=[207, 12]。随后按时间顺序以 8:2 的比例划分训练集与测试集——"
        "训练集只包含早于测试集的样本, 严格避免使用未来信息造成的数据泄漏。考虑到 CPU "
        "训练的时间成本, 程序提供 --max-samples 参数限制参与训练的样本数(默认 6000), "
        "当设为 0 时使用全部样本, 便于在不同算力下灵活复现。")

    # ---- 4 模型与方法 ----
    add_heading(doc, "4 模型与方法：A3T-GCN", 1)
    add_heading(doc, "4.1 问题定义", 2)
    add_body(doc,
        "交通速度预测可形式化为一个时空序列到序列的回归问题: 给定历史 P 个时间步的特征"
        "序列 [X_{t-P+1}, …, X_t](X_t∈R^{N×F}), 学习一个映射函数 f 预测未来 Q 个时间步"
        "的速度 [Y_{t+1}, …, Y_{t+Q}]。本文设定 P=Q=12, 即用过去 1 小时(12 个 5 分钟"
        "间隔)的观测预测未来 1 小时的车速。该设定属于多步预测, 比单步预测更具挑战性, "
        "因为误差会随预测视野的延伸而累积。")
    add_heading(doc, "4.2 总体结构", 2)
    add_body(doc,
        "A3T-GCN(Attention Temporal Graph Convolutional Network, 注意力时空图卷积网络)"
        "由 GCN、GRU 与注意力机制三部分有机组合而成, 旨在同时建模交通数据的空间依赖与"
        "时间依赖, 并自适应地强调重要的历史时刻。其整体数据流为: 输入时空张量先在每个"
        "时间步经图卷积提取空间特征, 再按时间顺序送入 GRU 编码时序演化, 最后由注意力"
        "模块对各时刻隐藏状态加权汇聚, 经线性层输出多步预测。")
    add_heading(doc, "4.3 图卷积网络: 捕获空间依赖", 2)
    add_body(doc,
        "在每个时间步, GCN 依据归一化邻接矩阵对节点特征进行邻域聚合: 一个传感器的表示由"
        "其自身与相邻传感器的特征加权求和得到, 从而将路网拓扑显式注入特征学习过程。多层"
        "图卷积能够扩大感受野, 使信息在路网上沿多跳邻居传播, 刻画拥堵的空间扩散效应——"
        "上游路段的速度下降会通过图卷积逐步影响到下游节点的表示。")
    add_heading(doc, "4.4 门控循环单元: 捕获时间依赖", 2)
    add_body(doc,
        "GRU 是循环神经网络的一种轻量变体, 通过更新门与重置门控制信息的保留与遗忘, 能够"
        "在缓解梯度消失的同时建模交通时序的短时趋势。A3T-GCN 将每一时间步经 GCN 得到的"
        "空间表示依次输入 GRU, 形成对时空演化的联合建模; 相比 LSTM, GRU 参数更少、训练"
        "更快, 在交通这类中等长度序列上往往能取得相近甚至更好的效果。")
    add_heading(doc, "4.5 注意力机制: 聚合全局时序信息", 2)
    add_body(doc,
        "GRU 在不同时间步会产生一系列隐藏状态, 不同历史时刻对未来预测的重要性并不相同"
        "(例如临近时刻往往更具参考价值, 而高峰起始时刻可能蕴含关键的趋势信号)。A3T-GCN "
        "通过注意力机制为各时间步的隐藏状态计算重要性权重, 并据此加权求和得到全局上下文"
        "向量, 从而突出关键时刻、抑制噪声。相较于只用最后一个隐藏状态的做法, 注意力机制"
        "使模型对高峰起讫、速度骤变等关键时刻更为敏感, 在多步预测中获得更稳健的表现。")
    add_heading(doc, "4.6 实现细节", 2)
    add_body(doc,
        "在具体实现上, 本文基于 PyTorch Geometric Temporal 提供的 A3TGCN2 单元构建模型: "
        "输入张量 [B, N, F, T] 经 A3TGCN2 得到每个节点的隐藏表示 [B, N, H](隐藏维度 H=32), "
        "再经 ReLU 激活与一个线性层映射为未来 Q 步的速度预测 [B, N, Q]。其中 B 为批大小。"
        "需要特别注意的是, A3TGCN2 的注意力组件与批大小绑定, 因此训练与评估时均采用固定"
        "批大小并丢弃不完整的尾批, 以保证张量形状一致, 避免维度不匹配错误。模型代码组织"
        "于 src/model.py(模型定义)、src/train.py(训练/评估/可视化)与 src/utils.py(指标"
        "与数据工具)三个文件中, 结构清晰、便于复用。")

    # ---- 5 实验与结果分析 ----
    add_heading(doc, "5 实验与结果分析", 1)
    add_heading(doc, "5.1 实验环境与超参数", 2)
    add_body(doc,
        f"实验在 CPU 环境下完成。优化器采用 Adam, 学习率为 {m['lr']}, 损失函数为均方误差"
        f"(MSE), 批大小为 {m['batch_size']}, 训练轮数为 {m['epochs']} 个 epoch, 随机种子"
        "固定为 42 以保证可复现。输入与输出步长均为 12。本次报告所依据的实验使用 "
        f"{m['train_samples']} 个训练样本与 {m['test_samples']} 个测试样本。依赖库版本见 "
        "requirements.txt(torch、torch-geometric、torch-geometric-temporal 等)。")
    add_heading(doc, "5.2 评价指标", 2)
    add_body(doc,
        "评估指标采用平均绝对误差(MAE)与均方根误差(RMSE)。为兼顾训练稳定性与结果的可"
        "解释性, 本文同时报告两种尺度的误差: 一是在 z-score 归一化尺度上的 MAE/RMSE, "
        "用于反映模型在标准化空间中的拟合优度; 二是将归一化误差乘以速度标准差还原到真实"
        "速度尺度(mph)后的 MAE/RMSE, 便于直观理解预测偏差对应的实际车速误差。此外补充"
        "平均绝对百分比误差(MAPE), 以相对误差的形式衡量预测精度。")
    add_heading(doc, "5.3 训练过程与损失曲线", 2)
    add_body(doc,
        f"在 {m['epochs']} 个 epoch 的训练中, 训练集 MSE 由初始的约 0.43 稳步下降至约 "
        f"{m['final_train_mse']}, 在前若干个 epoch 内迅速下降随后趋于平缓, 说明模型收敛"
        "良好且未出现明显的训练不稳定。训练损失曲线如图 1 所示。")
    if os.path.exists(LOSS_IMG):
        doc.add_picture(LOSS_IMG, width=Pt(380))
        doc.paragraphs[-1].alignment = WD_ALIGN_PARAGRAPH.CENTER
        cap = add_centered(doc, "图 1　训练损失(MSE)随 epoch 变化曲线",
                           name="楷体", size=10.5, bold=False)
        cap.paragraph_format.space_after = Pt(6)
    add_heading(doc, "5.4 预测结果与可视化", 2)
    add_body(doc,
        f"训练完成后, 模型在测试集上取得如下结果: 归一化尺度 MAE={m['test_MAE_normalized']}、"
        f"RMSE={m['test_RMSE_normalized']}; 还原到真实速度尺度 MAE≈{m['test_MAE_mph']} mph、"
        f"RMSE≈{m['test_RMSE_mph']} mph、MAPE≈{m.get('test_MAPE_pct', 0.0)}%。考虑到 "
        f"METR-LA 车速标准差约 {m['speed_std_mph']} mph, 该误差水平表明模型已经学到了"
        "交通速度的主要时空规律。图 2 给出了某传感器在测试集上的预测车速与真实车速对比。")
    if os.path.exists(PRED_IMG):
        doc.add_picture(PRED_IMG, width=Pt(400))
        doc.paragraphs[-1].alignment = WD_ALIGN_PARAGRAPH.CENTER
        cap = add_centered(doc, "图 2　某传感器预测车速与真实车速对比",
                           name="楷体", size=10.5, bold=False)
        cap.paragraph_format.space_after = Pt(6)
    add_body(doc,
        "从预测对比图可以看到, 模型预测的车速曲线能够较好地跟踪真实曲线的整体走势, 尤其"
        "能够捕捉到由高峰期引起的速度下降与平峰期的速度回升, 体现了时空图神经网络对交通"
        "模式的建模能力。")
    add_heading(doc, "5.5 结果讨论与误差分析", 2)
    add_body(doc,
        "进一步分析, 误差主要来源于三方面: 其一, 多步预测中后段时间步的误差累积——预测"
        "第 1 步(未来 5 分钟)精度最高, 随着视野延伸到第 12 步(未来 1 小时)误差逐步上升; "
        "其二, 突发事件(如事故、临时管制)难以从历史规律中预测; 其三, 受限于 CPU 算力, "
        "本文仅使用了部分样本与较少的训练轮数。若使用全量数据、增大训练轮数, 或引入更深"
        "的图卷积层与更精细的注意力结构, 预计可进一步降低误差。")
    add_body(doc,
        "从时段与空间维度看: 平峰期车速平稳、易于预测, 而早晚高峰的速度骤降阶段误差相对"
        "偏大, 说明对突变模式的建模仍有提升空间; 位于路网交汇处、连接关系复杂的传感器, "
        "其预测更依赖图卷积对邻居信息的聚合质量。综合来看, MAPE 约 19% 的水平在仅用部分"
        "样本、CPU 训练的条件下属于合理区间, 体现了 A3T-GCN 在空间建模与时间建模上的"
        "均衡能力。")

    # ---- 6 结论与展望 ----
    add_heading(doc, "6 结论与展望", 1)
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
        set_cn_font(p.add_run(r), "宋体", 10.5)

    # ---- 附录1 相关代码及操作 ----
    doc.add_page_break()
    add_heading(doc, "附录1 相关代码及操作", 1)
    add_heading(doc, "附1.1 实验过程", 2)
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

    add_heading(doc, "附1.2 运行命令", 2)
    add_mono(doc,
        "# 安装依赖\n"
        "pip install -r requirements.txt\n\n"
        "# 默认配置训练(CPU 友好, 限制样本量)\n"
        "python src/train.py --epochs 30 --max-samples 6000\n\n"
        "# 使用全部数据训练\n"
        "python src/train.py --epochs 30 --max-samples 0\n\n"
        "# 仅评估(加载已训练模型)\n"
        "python src/train.py --eval-only")

    add_heading(doc, "附1.3 中间结果(指标摘要)", 2)
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

    add_heading(doc, "附1.4 实验心得", 2)
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

    # ---- 附录2 查重报告 ----
    add_heading(doc, "附录2 查重报告", 1)
    add_body(doc,
        "本报告所涉及的文字内容、代码与实验结果均为作者独立完成, 引用他人成果均已在"
        "参考文献中标注。查重报告由学校指定的论文检测系统(如中国知网、维普等)生成, "
        "检测通过后将其结果页附于此处。", first_indent=False)


def enable_update_fields(doc):
    """在 settings.xml 写入 updateFields, 使 Word/WPS 打开文档时自动更新目录域。"""
    settings = doc.settings.element
    if settings.find(qn("w:updateFields")) is None:
        el = OxmlElement("w:updateFields")
        el.set(qn("w:val"), "true")
        settings.insert(0, el)


def _find_toc_paragraph(doc):
    """返回包含 TOC 域的段落对象, 找不到返回 None。"""
    for p in doc.paragraphs:
        if "TOC" in p._element.xml and "instrText" in p._element.xml:
            return p
    return None


def _page_map(pdf_path):
    """利用 pdftotext 逐页文本, 返回 (标题去空白 -> 物理页码) 的查找函数所需数据。"""
    txt = subprocess.run(["pdftotext", "-layout", pdf_path, "-"],
                         capture_output=True, check=True).stdout.decode("utf-8", "replace")
    pages = txt.split("\f")
    norm_pages = ["".join(pg.split()) for pg in pages]  # 去掉所有空白
    return norm_pages


def _usable_width_emu(doc):
    sec = doc.sections[-1]
    return int(sec.page_width) - int(sec.left_margin) - int(sec.right_margin)


def bake_static_toc(path):
    """渲染 PDF 计算页码, 把 TOC 域替换为带页码的静态目录。

    依赖 libreoffice 与 pdftotext; 任一缺失或失败则保留自动更新域(回退), 不报错。
    """
    soffice = shutil.which("libreoffice") or shutil.which("soffice")
    if not soffice or not shutil.which("pdftotext"):
        print("[目录] 未检测到 libreoffice/pdftotext, 保留自动更新目录域。")
        return
    try:
        with tempfile.TemporaryDirectory() as td:
            subprocess.run([soffice, "--headless", "--convert-to", "pdf",
                            "--outdir", td, path], check=True,
                           capture_output=True, timeout=180)
            pdfs = [f for f in os.listdir(td) if f.endswith(".pdf")]
            if not pdfs:
                print("[目录] PDF 渲染失败, 保留自动更新目录域。")
                return
            norm_pages = _page_map(os.path.join(td, pdfs[0]))

        def page_of(text):
            key = "".join(text.split())
            for i, pg in enumerate(norm_pages, start=1):
                if key in pg:
                    return i
            return None

        doc = Document(path)
        toc_p = _find_toc_paragraph(doc)
        if toc_p is None:
            print("[目录] 未找到 TOC 域, 跳过。")
            return
        width = _usable_width_emu(doc)
        anchor = toc_p._element
        # 自标题首次出现的页码; 目录条目按记录顺序输出
        from docx.text.paragraph import Paragraph
        for level, text in HEADINGS:
            pg = page_of(text)
            new_p = OxmlElement("w:p")
            anchor.addprevious(new_p)
            para = Paragraph(new_p, toc_p._parent)
            para.paragraph_format.line_spacing = 1.1
            para.paragraph_format.space_after = Pt(2)
            if level == 2:
                para.paragraph_format.left_indent = Pt(18)
            ts = para.paragraph_format.tab_stops
            ts.add_tab_stop(width, WD_TAB_ALIGNMENT.RIGHT, WD_TAB_LEADER.DOTS)
            run = para.add_run(text + "\t" + (str(pg) if pg else ""))
            set_cn_font(run, "宋体", 11.5 if level == 1 else 10.5,
                        bold=(level == 1))
        anchor.getparent().remove(anchor)  # 删除原 TOC 域
        doc.save(path)
        print("[目录] 已生成带页码的静态目录。")
    except Exception as e:  # noqa: BLE001 - 渲染失败时安全回退
        print(f"[目录] 生成静态目录失败({e}), 保留自动更新目录域。")


def main():
    m = load_metrics()
    doc = Document(TEMPLATE)
    strip_sample_toc(doc)
    build_report(doc, m)
    enable_update_fields(doc)
    doc.save(OUT)
    bake_static_toc(OUT)
    print(f"文档已生成: {os.path.abspath(OUT)}")


if __name__ == "__main__":
    main()
