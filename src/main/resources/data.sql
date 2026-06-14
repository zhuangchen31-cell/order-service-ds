-- 初始化管理员账号 (密码: 123456)
INSERT INTO users (username, password, email, phone, nickname, status)
SELECT 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKFUi', 'admin@ecommerce.com', '13800000001', '管理员', 1
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

-- 初始化测试用户 (密码: 123456)
INSERT INTO users (username, password, email, phone, nickname, status)
SELECT 'test', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKFUi', 'test@ecommerce.com', '13800000002', '测试用户', 1
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'test');

-- 初始化示例订单
INSERT INTO orders (order_no, user_id, total_amount, status, shipping_address, receiver_name, receiver_phone, remark)
SELECT 'ORD2024010001', 1, 299.99, 1, '北京市朝阳区建国路88号', '张三', '13800138001', '请尽快发货'
WHERE NOT EXISTS (SELECT 1 FROM orders WHERE order_no = 'ORD2024010001');

INSERT INTO orders (order_no, user_id, total_amount, status, shipping_address, receiver_name, receiver_phone)
SELECT 'ORD2024010002', 1, 159.50, 0, '北京市海淀区中关村大街1号', '张三', '13800138001'
WHERE NOT EXISTS (SELECT 1 FROM orders WHERE order_no = 'ORD2024010002');

INSERT INTO orders (order_no, user_id, total_amount, status, shipping_address, receiver_name, receiver_phone, remark)
SELECT 'ORD2024010003', 1, 899.00, 2, '上海市浦东新区世纪大道100号', '李四', '13900139002', '小心轻放'
WHERE NOT EXISTS (SELECT 1 FROM orders WHERE order_no = 'ORD2024010003');

-- 初始化商品数据（供商品CRUD接口测试）
INSERT INTO product (name, category_id, category_name, price, original_price, stock, sales, image, description, status)
SELECT 'iPhone 15 Pro', 1, '电子产品', 7999.00, 8999.00, 100, 520, 'https://picsum.photos/200?random=1', 'Apple旗舰手机，A17 Pro芯片', 1
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'iPhone 15 Pro');

INSERT INTO product (name, category_id, category_name, price, original_price, stock, sales, image, description, status)
SELECT 'MacBook Air M3', 1, '电子产品', 8999.00, 9499.00, 50, 230, 'https://picsum.photos/200?random=2', '轻薄笔记本，M3芯片', 1
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'MacBook Air M3');

INSERT INTO product (name, category_id, category_name, price, original_price, stock, sales, image, description, status)
SELECT '华为Mate 60 Pro', 1, '电子产品', 6999.00, 6999.00, 80, 890, 'https://picsum.photos/200?random=3', '华为旗舰手机，卫星通话', 1
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = '华为Mate 60 Pro');

INSERT INTO product (name, category_id, category_name, price, original_price, stock, sales, image, description, status)
SELECT '蓝牙降噪耳机', 1, '电子产品', 599.00, 799.00, 200, 1200, 'https://picsum.photos/200?random=4', '主动降噪，40小时续航', 1
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = '蓝牙降噪耳机');

INSERT INTO product (name, category_id, category_name, price, original_price, stock, sales, image, description, status)
SELECT '男士运动T恤', 2, '服装鞋帽', 129.00, 199.00, 300, 560, 'https://picsum.photos/200?random=5', '纯棉透气，亲肤舒适', 1
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = '男士运动T恤');

INSERT INTO product (name, category_id, category_name, price, original_price, stock, sales, image, description, status)
SELECT '女士羽绒服', 2, '服装鞋帽', 899.00, 1599.00, 60, 180, 'https://picsum.photos/200?random=6', '90%白鹅绒，保暖轻盈', 1
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = '女士羽绒服');

INSERT INTO product (name, category_id, category_name, price, original_price, stock, sales, image, description, status)
SELECT '实木餐桌', 3, '家居用品', 2599.00, 3599.00, 15, 42, 'https://picsum.photos/200?random=7', '北美白橡木，环保漆面', 1
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = '实木餐桌');

INSERT INTO product (name, category_id, category_name, price, original_price, stock, sales, image, description, status)
SELECT '有机绿茶礼盒', 4, '食品饮料', 288.00, 368.00, 150, 670, 'https://picsum.photos/200?random=8', '明前采摘，清香回甘', 1
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = '有机绿茶礼盒');

INSERT INTO product (name, category_id, category_name, price, original_price, stock, sales, image, description, status)
SELECT 'Python编程入门', 5, '图书文具', 59.00, 79.00, 500, 2300, 'https://picsum.photos/200?random=9', '零基础学Python，畅销教程', 1
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'Python编程入门');

INSERT INTO product (name, category_id, category_name, price, original_price, stock, sales, image, description, status)
SELECT '专业篮球', 6, '运动户外', 299.00, 399.00, 120, 340, 'https://picsum.photos/200?random=10', '室内外通用，防滑耐磨', 1
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = '专业篮球');

INSERT INTO product (name, category_id, category_name, price, original_price, stock, sales, image, description, status)
SELECT '已下架测试商品', 1, '电子产品', 99.00, 199.00, 0, 0, 'https://picsum.photos/200?random=99', '此商品已下架，用于测试', 0
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = '已下架测试商品');

-- 初始化示例订单明细
INSERT INTO order_item (order_id, product_id, product_name, sku_id, sku_code, price, quantity, sub_total)
SELECT 1, 1001, 'iPhone15 Pro', 2001, 'SKU-IP15P-256', 7999.00, 1, 7999.00
WHERE NOT EXISTS (SELECT 1 FROM order_item WHERE order_id = 1 AND product_id = 1001);

INSERT INTO order_item (order_id, product_id, product_name, sku_id, sku_code, price, quantity, sub_total)
SELECT 2, 1003, '小米手环8', 2003, 'SKU-M8-ST', 299.50, 1, 299.50
WHERE NOT EXISTS (SELECT 1 FROM order_item WHERE order_id = 2 AND product_id = 1003);

INSERT INTO order_item (order_id, product_id, product_name, sku_id, sku_code, price, quantity, sub_total)
SELECT 3, 1004, 'MacBook Pro 14', 2004, 'SKU-MBP14-M3', 12999.00, 1, 12999.00
WHERE NOT EXISTS (SELECT 1 FROM order_item WHERE order_id = 3 AND product_id = 1004);
