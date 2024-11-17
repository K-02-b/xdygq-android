# 养鸽器

## [X岛](https://www.nmbxd.com)专属应用

### 使用java编写，功能仅完成主体功能，有概率闪退

### 请手动关闭该应用的电量优化，打开该应用的后台访问网络权限

### Android13及以上需要手动开启通知权限

## 公告：

 - 反馈邮箱：[kainas@foxmail.com](mailto:kainas@foxmail.com)

## 已知BUG：

 - 使用一段时间后有概率无限闪退，无法再次进入应用（因技术原因无法查明具体错误，现已尽量尝试修复）

 - 删除时有概率删除不应删除的串

 - 删除所有串后保存仍会显示一个串号为0的串

 - 修改串号后新回复可能计算为负数

## 功能：

 - 1.跟踪串回复数，更新时通知

 - 2.选择是否只看Po

 - 3.点击通知跳转到应用界面并将相应通知设为已读

## 未来计划：

 - 0.修复BUG

 - 1.添加/修改串号后首次更新时计新回复为0且不进行通知通知

 - 1.应用饼干以访问部分板块

 - 2.点击通知栏的通知跳转到岛客户端（现为将相应串的通知设为已读）或在客户端内放置跳转按钮

 - 3.更美观的UI

 - 4.将配置提交到服务器备份

 - 5.添加可选功能「提交到服务器进行统一请求」，减少因重复请求同一串而给岛服务器带来的不必要的负载。此功能的核心在于客户端将追更列表提交给中转服务器，服务端整合所有客户端提交的列表并进行去重，确保相同的串不会被多次请求。理论上可以有效降低重复请求的数量，减轻岛服务器的压力，同时提升整体的响应速度
 
 - 6.编辑列表时上下移动改变排列顺序

## 更新：

 - 2024.9.17 10:24：完善功能，优化设计

 - 2024.11.17 19:53：优化代码

 - 2024.11.17 22:01：修复部分闪退问题，添加列表滚动
