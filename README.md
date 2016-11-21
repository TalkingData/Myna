# Myna

Android 平台场景识别框架

[![GitHub license](http://og41w30k3.bkt.clouddn.com/apache2.svg)](./LICENSE)


![](http://p1.bqimg.com/562611/952bd822efce378b.png)
 

Myna 项目中包含一个测试 Demo 工程：demo-myna, 将该工程和 Myna 项目本身导入到 Android Studio 中，就可以开始调试了。

Myna 提供了两套接口：

- 面向开发者的接口：开发者只需要简单的接口调用，就能在应用程序中获取实时识别的用户行为状态。
- 面向数据科学家的接口：数据科学家可以很方便地添加新的识别算法，在运行时调整订阅的传感器类型、采样频率和采样时长，而无需关心 Android 系统相关的传感器数据订阅细节。

目前 Myna 可以识别下面三种行为类型：

1. On_Foot
2. In_Vehicle
3. Still

Myna 使用随机森林分类算法的一种开源实现--**Dice** 进行的实时用户行为识别：

- [Dice项目主页](http://www.dice4dm.com/)

- [Dice文档](http://www.dice4dm.com/doc/index.html)

Myna中内置了一个已经训练好的模型文件，在运行时加载。模型的 ROC 为：

![](http://p1.bqimg.com/562611/13d6243cab1e64d8.png)

## 集成文档

[快速集成文档](/QuickStart.md)

## Roadmap

2016 年 12 月

	1. 开源包含训练代码的 Android App 和数据集。
	2. 开源模型评价和 Plot 的 Python 代码。
	3. 增加手持状态检测能力。
	3. 增加对更多行为的识别能力。

2017 年

	1. 加入更多机器学习算法。
	2. 移植 Tensorflow 的 CNN 实现到 Android 端。

## License

开源协议： [Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0)

	Copyright 2016 TalkingData
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	  http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.