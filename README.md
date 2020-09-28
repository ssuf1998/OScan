# OScan

![author_ssuf1998](https://img.shields.io/badge/author-ssuf1998-red)

📸 一个小巧的文档扫描仪，类似于“扫描全能王”。***该项目仍在开发中，还用不了呢。***

#### 主要原理
通过一种能够最大限度去除不均匀光照的影响的图像二值化算法（ [论文地址](https://kns.cnki.net/kcms/detail/detail.aspx?dbcode=CJFD&dbname=CJFD2011&filename=SGCJ201102019&v=cjS43dR%25mmd2FHLikFkWfG65ivlzDmM4E8Zw6o%25mmd2BI2yPFKW3YppEPVdcKRenPxIDLUepPA) ） ，首先进行二值化，之后再通过轮廓检测，凸包检测，以及利用重心求解矩形四角的方法，最终可以较为良好地解出图片中文档的位置。
                             
后面的操作就是很传统的，用户参与调整裁剪，透视变换，简单二值化或者使用ps中的高斯模糊+划分的方法，之后可以考虑尝试灰度世界算法来尽量减轻背景、光照颜色对文档呈现的影响。

对比市面上的扫描解析软件，本算法最大的缺点在于速度不够快，至于识别效果，没有大量数据就下结论似乎不具有说服力……

#### 想弄下来玩玩？
用了opencv4.4.0，要弄下来玩需要手动引入opencv的java库和共享库文件，具体看 [这篇文章](https://www.jianshu.com/p/6e16c0429044) 吧。

#### 有问题想交流？
邮箱：[ssuf1998@126.com](mailto:ssuf1998@126.com)

微信号：ssuf1998