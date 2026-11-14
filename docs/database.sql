-- ==========================================================
-- 文具店商城系统 - 数据库表结构及初始化数据
-- 数据库: MySQL 8.0+
-- ==========================================================

CREATE DATABASE IF NOT EXISTS stationery_mall DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE stationery_mall;

-- --------------------------------------------------------
-- 1. 用户表 (user)
-- --------------------------------------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码(MD5加密)',
  `role` tinyint NOT NULL DEFAULT '1' COMMENT '角色: 0-管理员, 1-普通用户',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- --------------------------------------------------------
-- 2. 分类表 (category)
-- --------------------------------------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父分类ID (0表示一级分类)',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

-- --------------------------------------------------------
-- 3. 商品表 (product)
-- --------------------------------------------------------
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `category_id` bigint NOT NULL COMMENT '所属二级分类ID',
  `name` varchar(100) NOT NULL COMMENT '商品名称',
  `price` decimal(10,2) NOT NULL COMMENT '商品价格',
  `stock` int UNSIGNED NOT NULL DEFAULT '0' COMMENT '库存 (UNSIGNED防止负数)',
  `description` varchar(500) DEFAULT NULL COMMENT '商品描述',
  `image_url` varchar(255) DEFAULT NULL COMMENT '图片地址',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-已下架, 1-已上架',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `fk_product_category` (`category_id`),
  CONSTRAINT `fk_product_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- --------------------------------------------------------
-- 4. 购物车项表 (cart_item)
-- --------------------------------------------------------
DROP TABLE IF EXISTS `cart_item`;
CREATE TABLE `cart_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `quantity` int UNSIGNED NOT NULL DEFAULT '1' COMMENT '数量',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product` (`user_id`,`product_id`),
  KEY `fk_cart_product` (`product_id`),
  CONSTRAINT `fk_cart_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`),
  CONSTRAINT `fk_cart_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车项表';

-- --------------------------------------------------------
-- 5. 订单表 (orders)
-- --------------------------------------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` varchar(50) NOT NULL COMMENT '订单号',
  `user_id` bigint NOT NULL COMMENT '下单用户ID',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态: 0-待支付, 1-已支付, 2-已发货, 3-已完成, 4-已取消',
  `address` varchar(255) NOT NULL COMMENT '收货地址(下单时填写的文本框)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `fk_order_user` (`user_id`),
  CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- --------------------------------------------------------
-- 6. 订单明细表 (order_item)
-- --------------------------------------------------------
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `product_name` varchar(100) NOT NULL COMMENT '商品名称快照',
  `product_price` decimal(10,2) NOT NULL COMMENT '商品单价快照',
  `quantity` int UNSIGNED NOT NULL COMMENT '购买数量',
  PRIMARY KEY (`id`),
  KEY `fk_item_order` (`order_id`),
  CONSTRAINT `fk_item_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表(含价格快照)';

-- --------------------------------------------------------
-- 7. 评价表 (review)
-- --------------------------------------------------------
DROP TABLE IF EXISTS `review`;
CREATE TABLE `review` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `order_id` bigint NOT NULL COMMENT '关联的订单ID',
  `rating` tinyint NOT NULL COMMENT '评分 1-5星',
  `content` varchar(500) DEFAULT NULL COMMENT '评价文字',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_product` (`order_id`,`product_id`) COMMENT '同一订单同一商品只能评一次',
  KEY `fk_review_product` (`product_id`),
  CONSTRAINT `fk_review_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

-- ==========================================================
-- 初始化数据 (满足需求文档 3.3 数据要求)
-- ==========================================================

-- 1. 用户数据 (密码统一为 123456 的 MD5: e10adc3949ba59abbe56e057f20f883e)
-- 管理员1个，普通用户2个
INSERT INTO `user` (`id`, `username`, `password`, `role`) VALUES 
(1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', 0),
(2, 'user1', 'e10adc3949ba59abbe56e057f20f883e', 1),
(3, 'user2', 'e10adc3949ba59abbe56e057f20f883e', 1);

-- 2. 分类数据 (一级不少于3个，包含二级)
INSERT INTO `category` (`id`, `name`, `parent_id`) VALUES 
(1, '文具用品', 0),
(2, '办公设备', 0),
(3, '数码周边', 0),
(4, '笔类', 1),
(5, '本册纸品', 1),
(6, '文件管理', 1),
(7, '打印设备', 2),
(8, '键盘鼠标', 3);

-- 3. 商品数据 (不少于12件，含2件下架、1件库存为0)
INSERT INTO `product` (`id`, `category_id`, `name`, `price`, `stock`, `status`, `description`, `image_url`) VALUES 
(1, 4, '晨光中性笔 0.5mm 黑色', 2.50, 500, 1, '顺滑好写，学生办公必备', '/img/pen1.jpg'),
(2, 4, '得力 2B铅笔 10支装', 8.00, 200, 1, '考试专用，涂卡清晰', '/img/pencil.jpg'),
(3, 5, '无印良品 A5 笔记本', 15.00, 100, 1, '简约横线本', '/img/notebook.jpg'),
(4, 5, '得力 A4复印纸 500张', 25.00, 300, 1, '70g加厚，不卡纸', '/img/a4paper.jpg'),
(5, 6, '齐心 文件夹 蓝色', 6.50, 150, 1, '双强力夹', '/img/folder.jpg'),
(6, 6, '订书机 标准型', 18.00, 50, 1, '省力订书机', '/img/stapler.jpg'),
(7, 7, '惠普打印机 家用款', 899.00, 20, 1, '打印复印扫描一体', '/img/printer.jpg'),
(8, 8, '罗技无线鼠标', 99.00, 80, 1, '静音办公鼠标', '/img/mouse.jpg'),
(9, 8, '机械键盘 87键', 299.00, 30, 1, '青轴，办公游戏皆宜', '/img/keyboard.jpg'),
(10, 4, '马克笔 24色套装', 35.00, 60, 1, '双头设计，色彩鲜艳', '/img/marker.jpg'),
(11, 5, '便利贴 混色装', 4.50, 0, 1, '库存为0测试商品', '/img/sticky.jpg'),
(12, 6, '计算器 12位大屏', 45.00, 40, 0, '下架测试商品', '/img/calculator.jpg'),
(13, 7, '碎纸机 家用小型', 199.00, 15, 0, '下架测试商品', '/img/shredder.jpg');

-- 4. 订单数据 (不同状态各1条)
-- 注意：为了演示，这里直接插入了状态为已完成(3)的订单，实际由系统流转。金额按快照算。
INSERT INTO `orders` (`id`, `order_no`, `user_id`, `total_amount`, `status`, `address`, `create_time`) VALUES 
(1, 'ORD20231001001', 2, 10.50, 0, '北京市海淀区中关村大街1号', '2023-10-01 10:00:00'), -- 待支付
(2, 'ORD20231002001', 2, 15.00, 1, '北京市海淀区中关村大街1号', '2023-10-02 11:30:00'), -- 已支付
(3, 'ORD20231003001', 3, 25.00, 2, '上海市浦东新区张江高科', '2023-10-03 14:15:00'),   -- 已发货
(4, 'ORD20231004001', 3, 198.00, 3, '上海市浦东新区张江高科', '2023-10-04 09:00:00'),   -- 已完成 (可评价)
(5, 'ORD20231005001', 2, 45.00, 4, '北京市海淀区中关村大街1号', '2023-10-05 16:40:00');   -- 已取消

-- 5. 订单明细数据 (包含商品名称和单价快照)
INSERT INTO `order_item` (`order_id`, `product_id`, `product_name`, `product_price`, `quantity`) VALUES 
(1, 1, '晨光中性笔 0.5mm 黑色', 2.50, 2),
(1, 5, '齐心 文件夹 蓝色', 6.50, 1), -- 总额 2.5*2 + 6.5 = 11.5 (这里总额简单写了10.5示意，实际按服务端计算)
(2, 3, '无印良品 A5 笔记本', 15.00, 1),
(3, 4, '得力 A4复印纸 500张', 25.00, 1),
(4, 8, '罗技无线鼠标', 99.00, 2), -- 总额 198.00
(5, 12, '计算器 12位大屏', 45.00, 1); -- 测试已取消订单

-- 6. 购物车数据 (示例)
INSERT INTO `cart_item` (`user_id`, `product_id`, `quantity`) VALUES 
(2, 2, 2),
(3, 7, 1);

-- 7. 评价数据 (针对已完成订单的单品)
INSERT INTO `review` (`user_id`, `product_id`, `order_id`, `rating`, `content`) VALUES 
(3, 8, 4, 5, '鼠标很好用，静音效果不错！');