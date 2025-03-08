# 须知
* WU: 世界单位, 8 WU = 1 格。 可用小数。
* Text: 格式: `<text here>`. 替换 `\n` 为 `[n]`。
* Team: 队伍名 (derelict/sharded/crux/malis) 或队伍id。
* UnitType: 单位内部名 (dagger, new-horizon-branch for example) 或单位id。

***
# 可用Action
### 特殊操作
* `wait`: 等候指定时长。

  可用变量:
    * [1] (second)时间: 等候的时间。
### 相机操作
* `camera-control`: 移动相机至指定位置。

  可用变量:
    * [1] (second)时间: 相机移动时间
    * [2] (WU)x: 相机移动的 x 坐标
    * [3] (WU)y: 相机移动的 y 坐标


* `camera-reset`: 复位相机至玩家

  可用变量:
    * [1] (second)时间: 相机复位时间

* `camera-set`: 将相机直接移动到指定坐标

  可用变量:
    * [1] (WU)x: 相机移动的 x 坐标
    * [2] (WU)y: 相机移动的 y 坐标
### 幕布/背景
* `curtain-draw`: 拉下帷幕。固定1.5秒

* `curtain-raise`: 拉开帷幕。固定1.5秒

* `curtain-fade-in`: 屏幕淡入。固定2秒

* `curtain-fade-out`: 屏幕淡出。固定2秒
### COD样式信息显示
* `info-fade-in`: 信息淡入。固定0.25秒

* `info-fade-out`: 信息淡出。固定0.25秒。清空信息文本。

* `info-text`: 信息文本。

  可用变量:
    * [1] (Text)文本: COD样式的文本.
### 信号切入式对话框
* `signal-cut-in`: 信号切入。固定0.5秒

* `signal-cut-out`: 信号切出。固定0.5秒

* `signal-text`: 信号文本

  可用变量:
    * [1] (Text)text: 文本内容
### 输入
* `input-lock`: 锁定输入

* `input-unlock`: 解锁输入
### 世界事件
* `jump-in`: 召唤一个单位跃迁入场

  可用变量:
    * [1] (UnitType)单位: 入场单位
    * [2] (Team)队伍: 入场单位的队伍
    * [3] (WU)x: 入场 x 坐标
    * [4] (WU)y: 入场 y 坐标
    * [5] (Float)角度: 入场角度
    * [6] (Second)时延: 入场时延
    * [7] (WU)范围: 入场范围


* `mark-world`: 在地图上创建标记

  可用变量:
    * [1] (WU)x: 标记 x 坐标
    * [2] (WU)y: 标记 y 坐标
    * [3] (WU)范围: 标记范围
    * [4] (Second)时长: 标记时长
    * [5] (Integer)样式: 标记样式
        * 默认: 默认
        * 1: 默认(无线条)
        * 2: 默认(固定动画)
        * 3: 震动信号

* `raid`: 在指定位置处创建空袭

  可用变量:
    * [1] (Team)队伍: 子弹队伍
    * [2] (Integer)子弹: 空袭子弹类型
        * 默认: 默认空袭(溅射伤害500, 溅射范围60)
    * [3] (WU)空袭源x: 空袭源的x坐标
    * [4] (WU)空袭源y: 空袭源的y坐标
    * [3] (WU)目标x: 目标的x坐标
    * [4] (WU)目标y: 目标的y坐标
    * [7] (WU)范围: 空袭创建坐标随机范围

### 警报
* `warning-icon`: 警报HUD

  可用变量:
    * [1] (Integer)icon: 警告icon
        * 默认: objective
        * 1: raid
        * 2: fleet
        * 3: capture
    * [2] (Team)队伍: 用于hud的队伍颜色
    * [3] (Text)文本: 警报文本

* `warning-sound`: 警报声

  可用变量:
    * [1] (Integer)友方警报: 用于友方玩家的警报
    * [2] (Integer)敌方警报: 用于敌方玩家的警报
    * [3] (Team)队伍: 警报源的队伍