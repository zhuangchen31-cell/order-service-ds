-- 初始化用户数据（密码为BCrypt加密后的 "123456"）
INSERT INTO users (username, password, email, phone, nickname, status) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', 'admin@example.com', '13800138000', '管理员', 1),
('user1', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', 'user1@example.com', '13800138001', '张三', 1),
('user2', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', 'user2@example.com', '13900139002', '李四', 1);

-- 初始化订单数据
INSERT INTO orders (order_no, user_id, total_amount, status, shipping_address, receiver_name, receiver_phone, remark) VALUES
('ORD2024010001', 2, 299.99, 1, '北京市朝阳区建国路88号', '张三', '13800138001', '请尽快发货'),
('ORD2024010002', 2, 159.50, 0, '北京市海淀区中关村大街1号', '张三', '13800138001', NULL),
('ORD2024010003', 3, 899.00, 2, '上海市浦东新区世纪大道100号', '李四', '13900139002', '小心轻放'),
('ORD2024010004', 2, 599.00, 1, '广州市天河区珠江新城', '张三', '13800138001', '周末送货');

-- 初始化订单商品明细
INSERT INTO order_item (order_id, product_id, product_name, sku_id, sku_code, price, quantity, sub_total) VALUES
(1, 1001, 'iPhone15 Pro', 2001, 'SKU-IP15P-256', 7999.00, 1, 7999.00),
(1, 1002, 'AirPods Pro', 2002, 'SKU-APP-2', 1999.00, 1, 1999.00),
(2, 1003, '小米手环8', 2003, 'SKU-M8-ST', 299.50, 1, 299.50),
(3, 1004, 'MacBook Pro 14', 2004, 'SKU-MBP14-M3', 12999.00, 1, 12999.00),
(4, 1005, '华为Mate60 Pro', 2005, 'SKU-MATE60-512', 6999.00, 1, 6999.00);
