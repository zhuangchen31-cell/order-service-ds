-- 用户数据（密码统一为 123456，BCrypt 加密）
INSERT INTO users (username, password, phone, email, role, enabled)
SELECT 'admin', '$2a$10$wgm8OHk2f6p1WqIufre9wOqajc559f/zg0.iQ06e9vGZlNI9RgArm', '13800000001', 'admin@ecommerce.com', 'ADMIN', 1
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

INSERT INTO users (username, password, phone, email, role, enabled)
SELECT 'test', '$2a$10$wgm8OHk2f6p1WqIufre9wOqajc559f/zg0.iQ06e9vGZlNI9RgArm', '13800000002', 'test@ecommerce.com', 'USER', 1
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'test');

-- 商品数据（12 款，图片与名称精准匹配）
-- 1. 无线蓝牙耳机
INSERT INTO products (name, category, description, image_url, price, stock, status)
SELECT '无线降噪蓝牙耳机 Pro', '数码', '主动降噪｜30小时超长续航｜Hi-Fi 音质｜IPX5 防水，通勤运动首选。', 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=600&q=80', 299.00, 128, 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = '无线降噪蓝牙耳机 Pro');

-- 2. 商务双肩包
INSERT INTO products (name, category, description, image_url, price, stock, status)
SELECT '都市商务双肩背包', '箱包', '防泼水面料｜15.6英寸电脑专属仓｜多隔层收纳｜人体工学背负。', 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=600&q=80', 169.00, 76, 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = '都市商务双肩背包');

-- 3. 电热水壶
INSERT INTO products (name, category, description, image_url, price, stock, status)
SELECT '智能恒温电热水壶', '家电', '1.7L 大容量｜五档精准控温｜304 食品级不锈钢｜自动断电保护。', 'https://images.unsplash.com/photo-1594213114663-d94db9b17125?auto=format&fit=crop&w=600&q=80', 129.00, 52, 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = '智能恒温电热水壶');

-- 4. 纯棉四件套
INSERT INTO products (name, category, description, image_url, price, stock, status)
SELECT 'A类母婴级纯棉四件套', '家居', '100% 新疆长绒棉｜亲肤透气不起球｜活性印染不褪色｜多色可选。', 'https://images.unsplash.com/photo-1584100936595-c0654b55a2e2?auto=format&fit=crop&w=600&q=80', 399.00, 33, 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'A类母婴级纯棉四件套');

-- 5. 筋膜枪
INSERT INTO products (name, category, description, image_url, price, stock, status)
SELECT '便携深层筋膜放松仪', '运动', '4 档力度调节｜超静音无刷电机｜Type-C 快充｜运动后快速恢复。', 'https://images.unsplash.com/photo-1571019613914-85f342c6a11e?auto=format&fit=crop&w=600&q=80', 219.00, 41, 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = '便携深层筋膜放松仪');

-- 6. 智能手机
INSERT INTO products (name, category, description, image_url, price, stock, status)
SELECT '旗舰影像智能手机 S24', '数码', '200MP 超清主摄｜120Hz 高刷屏｜5000mAh 大电池｜AI 智能助手。', 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&w=600&q=80', 4999.00, 65, 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = '旗舰影像智能手机 S24');

-- 7. 跑步鞋
INSERT INTO products (name, category, description, image_url, price, stock, status)
SELECT '飞织透气减震跑步鞋', '运动', '飞织网面透气不闷脚｜缓震中底回弹好｜橡胶大底防滑耐磨。', 'https://images.unsplash.com/photo-1542291026-7eec264c27da?auto=format&fit=crop&w=600&q=80', 459.00, 89, 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = '飞织透气减震跑步鞋');

-- 8. 机械键盘
INSERT INTO products (name, category, description, image_url, price, stock, status)
SELECT 'RGB 三模机械键盘 K8', '数码', 'Gasket 结构｜全键热插拔｜三模连接｜RGB 背光｜PBT 键帽。', 'https://images.unsplash.com/photo-1587829741301-dc7981a1c08e?auto=format&fit=crop&w=600&q=80', 349.00, 120, 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'RGB 三模机械键盘 K8');

-- 9. 台灯
INSERT INTO products (name, category, description, image_url, price, stock, status)
SELECT '智能护眼 LED 台灯', '家居', '无频闪无蓝光危害｜三档色温无极调光｜触摸控制｜USB 充电。', 'https://images.unsplash.com/photo-1507473885765-e6ed057ab6fe?auto=format&fit=crop&w=600&q=80', 189.00, 77, 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = '智能护眼 LED 台灯');

-- 10. 瑜伽垫
INSERT INTO products (name, category, description, image_url, price, stock, status)
SELECT '加厚防滑瑜伽垫 NBR', '运动', '10mm 加厚缓冲｜双面防滑纹理｜环保 NBR 材质｜含收纳绑带。', 'https://images.unsplash.com/photo-1601925260368-ae2f83cf8b7f?auto=format&fit=crop&w=600&q=80', 99.00, 156, 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = '加厚防滑瑜伽垫 NBR');

-- 11. 咖啡机
INSERT INTO products (name, category, description, image_url, price, stock, status)
SELECT '全自动意式咖啡机', '家电', '20Bar 高压萃取｜蒸汽奶泡管｜1.5L 水箱｜一键制作拿铁卡布奇诺。', 'https://images.unsplash.com/photo-1504630083234-11187a8dfb99?auto=format&fit=crop&w=600&q=80', 1299.00, 28, 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = '全自动意式咖啡机');

-- 12. 智能手表
INSERT INTO products (name, category, description, image_url, price, stock, status)
SELECT '全天候健康智能手表 GT5', '数码', '1.43英寸 AMOLED 屏｜心率血氧监测｜14 天续航｜50 米防水。', 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=600&q=80', 899.00, 43, 1
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = '全天候健康智能手表 GT5');

-- 订单数据
INSERT INTO orders (order_no, user_id, total_amount, status, shipping_address, receiver_name, receiver_phone, remark)
SELECT 'ORD20240601001', 2, 299.00, 1, '北京市朝阳区建国路 88 号 SOHO 现代城', '张三', '13800138001', '已付款，请尽快发货'
WHERE NOT EXISTS (SELECT 1 FROM orders WHERE order_no = 'ORD20240601001');

INSERT INTO orders (order_no, user_id, total_amount, status, shipping_address, receiver_name, receiver_phone)
SELECT 'ORD20240601002', 2, 586.00, 0, '北京市海淀区中关村大街 1 号', '张三', '13800138001'
WHERE NOT EXISTS (SELECT 1 FROM orders WHERE order_no = 'ORD20240601002');

INSERT INTO orders (order_no, user_id, total_amount, status, shipping_address, receiver_name, receiver_phone, remark)
SELECT 'ORD20240601003', 2, 1448.00, 2, '上海市浦东新区世纪大道 100 号环球金融中心', '李四', '13900139002', '小心轻放，易碎物品'
WHERE NOT EXISTS (SELECT 1 FROM orders WHERE order_no = 'ORD20240601003');

-- 订单明细（product_id 对应 products 表自增 ID 1-12）
INSERT INTO order_item (order_id, product_id, product_name, sku_id, sku_code, price, quantity, sub_total)
SELECT 1, 1, '无线降噪蓝牙耳机 Pro', 101, 'SKU-BT-PRO', 299.00, 1, 299.00
WHERE NOT EXISTS (SELECT 1 FROM order_item WHERE order_id = 1 AND product_id = 1);

INSERT INTO order_item (order_id, product_id, product_name, sku_id, sku_code, price, quantity, sub_total)
SELECT 2, 8, 'RGB 三模机械键盘 K8', 108, 'SKU-K8-RGB', 349.00, 1, 349.00
WHERE NOT EXISTS (SELECT 1 FROM order_item WHERE order_id = 2 AND product_id = 8);

INSERT INTO order_item (order_id, product_id, product_name, sku_id, sku_code, price, quantity, sub_total)
SELECT 2, 10, '加厚防滑瑜伽垫 NBR', 110, 'SKU-YOGA-NBR', 99.00, 1, 99.00
WHERE NOT EXISTS (SELECT 1 FROM order_item WHERE order_id = 2 AND product_id = 10);

INSERT INTO order_item (order_id, product_id, product_name, sku_id, sku_code, price, quantity, sub_total)
SELECT 3, 6, '旗舰影像智能手机 S24', 106, 'SKU-S24-256', 4999.00, 1, 4999.00
WHERE NOT EXISTS (SELECT 1 FROM order_item WHERE order_id = 3 AND product_id = 6);
