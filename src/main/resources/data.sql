-- 初始化管理员账号 (密码: 123456)
INSERT INTO users (username, password, phone, email, role, enabled)
SELECT 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKFUi', '13800000001', 'admin@ecommerce.com', 'ADMIN', 1
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

-- 初始化测试用户 (密码: 123456)
INSERT INTO users (username, password, phone, email, role, enabled)
SELECT 'test', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKFUi', '13800000002', 'test@ecommerce.com', 'USER', 1
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'test');

-- 初始化示例订单
INSERT INTO orders (order_no, user_id, total_amount, status, shipping_address, receiver_name, receiver_phone, remark)
SELECT 'ORD2024010001', 2, 299.99, 1, '北京市朝阳区建国路88号', '张三', '13800138001', '请尽快发货'
WHERE NOT EXISTS (SELECT 1 FROM orders WHERE order_no = 'ORD2024010001');

INSERT INTO orders (order_no, user_id, total_amount, status, shipping_address, receiver_name, receiver_phone)
SELECT 'ORD2024010002', 2, 159.50, 0, '北京市海淀区中关村大街1号', '张三', '13800138001'
WHERE NOT EXISTS (SELECT 1 FROM orders WHERE order_no = 'ORD2024010002');

INSERT INTO orders (order_no, user_id, total_amount, status, shipping_address, receiver_name, receiver_phone, remark)
SELECT 'ORD2024010003', 2, 899.00, 2, '上海市浦东新区世纪大道100号', '李四', '13900139002', '小心轻放'
WHERE NOT EXISTS (SELECT 1 FROM orders WHERE order_no = 'ORD2024010003');

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
