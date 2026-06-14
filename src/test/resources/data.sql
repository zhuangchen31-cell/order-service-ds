-- 测试数据
-- 测试用户 (密码: 123456, BCrypt加密)
INSERT INTO users (username, password, phone, email, role, enabled) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKFUi', '13800000000', 'admin@test.com', 'ADMIN', 1),
('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKFUi', '13900000000', 'test@test.com', 'USER', 1);

INSERT INTO orders (order_no, user_id, total_amount, status, shipping_address, receiver_name, receiver_phone, remark) VALUES
('ORD2024010001', 1, 299.99, 1, '北京市朝阳区建国路88号', '张三', '13800138001', '请尽快发货'),
('ORD2024010002', 1, 159.50, 0, '北京市海淀区中关村大街1号', '张三', '13800138001', NULL),
('ORD2024010003', 2, 899.00, 2, '上海市浦东新区世纪大道100号', '李四', '13900139002', '小心轻放');

INSERT INTO order_item (order_id, product_id, product_name, sku_id, sku_code, price, quantity, sub_total) VALUES
(1, 1001, 'iPhone15 Pro', 2001, 'SKU-IP15P-256', 7999.00, 1, 7999.00),
(1, 1002, 'AirPods Pro', 2002, 'SKU-APP-2', 1999.00, 1, 1999.00),
(2, 1003, '小米手环8', 2003, 'SKU-M8-ST', 299.50, 1, 299.50),
(3, 1004, 'MacBook Pro 14', 2004, 'SKU-MBP14-M3', 12999.00, 1, 12999.00);
