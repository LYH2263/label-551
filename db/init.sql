-- CakeShop database initialization
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    phone VARCHAR(20) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    username VARCHAR(50) NOT NULL,
    role ENUM('user', 'admin') DEFAULT 'user',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS addresses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    receiver_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    province VARCHAR(50) NOT NULL,
    city VARCHAR(50) NOT NULL,
    district VARCHAR(50) NOT NULL,
    detail VARCHAR(255) NOT NULL,
    is_default TINYINT(1) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS cakes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category_id INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    original_price DECIMAL(10,2),
    image VARCHAR(255),
    images TEXT,
    description TEXT,
    size VARCHAR(50),
    flavor VARCHAR(50),
    stock INT DEFAULT 0,
    sales INT DEFAULT 0,
    status ENUM('on', 'off') DEFAULT 'on',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS cart_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    cake_id INT NOT NULL,
    quantity INT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_cake (user_id, cake_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (cake_id) REFERENCES cakes(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(50) NOT NULL UNIQUE,
    user_id INT NOT NULL,
    address_id INT,
    receiver_name VARCHAR(50) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    receiver_address VARCHAR(500) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status ENUM('pending', 'paid', 'shipping', 'completed', 'cancelled') DEFAULT 'pending',
    remark VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    cake_id INT NOT NULL,
    cake_name VARCHAR(100) NOT NULL,
    cake_image VARCHAR(255),
    price DECIMAL(10,2) NOT NULL,
    quantity INT NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Users (admin password: admin123, user password: 123456)
INSERT INTO users (phone, password, username, role) VALUES
('13800000000', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', '管理员', 'admin'),
('13800000001', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '张三', 'user'),
('13800000002', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '李四', 'user');

INSERT INTO addresses (user_id, receiver_name, phone, province, city, district, detail, is_default) VALUES
(2, '张三', '13800000001', '广东省', '深圳市', '南山区', '科技园南路18号', 1),
(2, '张三', '13800000001', '广东省', '深圳市', '福田区', '华强北路100号', 0),
(3, '李四', '13800000002', '北京市', '北京市', '朝阳区', '望京SOHO T1 2001', 1);

INSERT INTO categories (name, description, sort_order) VALUES
('奶油蛋糕', '经典奶油蛋糕，口感细腻', 1),
('慕斯蛋糕', '轻盈柔滑的慕斯系列', 2),
('芝士蛋糕', '浓郁芝士，入口即化', 3),
('水果蛋糕', '新鲜水果，清爽好味', 4),
('巧克力蛋糕', '浓郁巧克力，甜蜜诱惑', 5),
('生日蛋糕', '生日定制与庆祝主题', 6),
('节日蛋糕', '节日氛围主题蛋糕', 7);

INSERT INTO cakes (name, category_id, price, original_price, image, description, size, flavor, stock, sales, status) VALUES
('经典奶油草莓蛋糕', 1, 168.00, 198.00, 'https://commons.wikimedia.org/wiki/Special:FilePath/Shortcake_slice.jpg?width=900', '新鲜草莓搭配奶油夹心，层次丰富。', '8寸', '草莓味', 50, 128, 'on'),
('香草奶油千层', 1, 188.00, 218.00, 'https://commons.wikimedia.org/wiki/Special:FilePath/Crepe_Cake_1_2019-12-24.jpg?width=900', '法式千层工艺，口感细腻。', '6寸', '香草味', 30, 86, 'on'),
('玫瑰奶油蛋糕', 1, 198.00, 238.00, 'https://commons.wikimedia.org/wiki/Special:FilePath/Cake_with_rose_2.jpg?width=900', '玫瑰花瓣装饰，适合浪漫场景。', '8寸', '玫瑰味', 25, 65, 'on'),
('芒果慕斯', 2, 158.00, 188.00, 'https://commons.wikimedia.org/wiki/Special:FilePath/Mango_mousse_cake_%288201823494%29.jpg?width=900', '芒果香气浓郁，口感清爽。', '6寸', '芒果味', 40, 156, 'on'),
('提拉米苏', 2, 178.00, 208.00, 'https://commons.wikimedia.org/wiki/Special:FilePath/Inside_of_a_tiramisu_cake.jpg?width=900', '经典意式风味，咖啡香浓。', '6寸', '咖啡味', 35, 142, 'on'),
('抹茶慕斯', 2, 168.00, 198.00, 'https://commons.wikimedia.org/wiki/Special:FilePath/Making_matcha_sponge_cake_at_home_04.jpg?width=900', '抹茶香气自然，不甜不腻。', '6寸', '抹茶味', 28, 98, 'on'),
('纽约重芝士', 3, 188.00, 228.00, 'https://commons.wikimedia.org/wiki/Special:FilePath/New_York_cheesecake_2.jpg?width=900', '奶香浓郁，芝士控必选。', '6寸', '原味', 20, 178, 'on'),
('轻乳酪蛋糕', 3, 148.00, 178.00, 'https://commons.wikimedia.org/wiki/Special:FilePath/Souffl%C3%A9-style_cheesecake_001.jpg?width=900', '轻盈绵密，入口即化。', '6寸', '原味', 45, 203, 'on'),
('蓝莓芝士', 3, 198.00, 238.00, 'https://commons.wikimedia.org/wiki/Special:FilePath/001_Blueberry_cheesecakes.jpg?width=900', '蓝莓果香与芝士完美搭配。', '8寸', '蓝莓味', 22, 89, 'on'),
('热带水果蛋糕', 4, 228.00, 268.00, 'https://commons.wikimedia.org/wiki/Special:FilePath/Fruit_Cake_001.jpg?width=900', '多种水果组合，清新爽口。', '8寸', '混合水果', 18, 76, 'on'),
('榴莲千层', 4, 268.00, 318.00, 'https://commons.wikimedia.org/wiki/Special:FilePath/Durian_cake.jpg?width=900', '榴莲果肉饱满，风味浓郁。', '8寸', '榴莲味', 15, 134, 'on'),
('黑森林蛋糕', 5, 178.00, 208.00, 'https://commons.wikimedia.org/wiki/Special:FilePath/Black_Forest_gateau.jpg?width=900', '德式经典，巧克力与樱桃搭配。', '8寸', '巧克力', 32, 167, 'on'),
('熔岩巧克力', 5, 198.00, 238.00, 'https://commons.wikimedia.org/wiki/Special:FilePath/Chocolate_lava_cake.jpg?width=900', '浓郁巧克力流心，口感醇厚。', '6寸', '巧克力', 25, 145, 'on'),
('生日快乐主题蛋糕', 6, 258.00, 308.00, 'https://commons.wikimedia.org/wiki/Special:FilePath/A_birthday_cake_2.jpg?width=900', '支持祝福语定制，多主题可选。', '10寸', '多口味可选', 50, 289, 'on'),
('儿童卡通蛋糕', 6, 288.00, 338.00, 'https://commons.wikimedia.org/wiki/Special:FilePath/Child%27s_Birthday_Cake_%288699482292%29.jpg?width=900', '卡通造型，适合亲子生日。', '8寸', '奶油味', 30, 156, 'on'),
('圣诞主题蛋糕', 7, 238.00, 288.00, 'https://commons.wikimedia.org/wiki/Special:FilePath/Christmas_cake_with_Christmas_verse.jpg?width=900', '圣诞主题装饰，节日氛围浓。', '8寸', '草莓味', 20, 45, 'on'),
('新年福蛋糕', 7, 268.00, 318.00, 'https://commons.wikimedia.org/wiki/Special:FilePath/Red_Velvet_Cake_Waldorf_Astoria.jpg?width=900', '新年主题设计，适合礼赠。', '10寸', '红丝绒', 15, 38, 'on');

INSERT INTO orders (order_no, user_id, receiver_name, receiver_phone, receiver_address, total_amount, status, remark) VALUES
('ORD20240101001', 2, '张三', '13800000001', '广东省深圳市南山区科技园南路18号', 356.00, 'completed', '生日蛋糕，请写祝福语'),
('ORD20240102001', 2, '张三', '13800000001', '广东省深圳市南山区科技园南路18号', 188.00, 'shipping', NULL),
('ORD20240103001', 3, '李四', '13800000002', '北京市朝阳区望京SOHO T1 2001', 268.00, 'pending', '下午3点前送达');

INSERT INTO order_items (order_id, cake_id, cake_name, cake_image, price, quantity, subtotal) VALUES
(1, 1, '经典奶油草莓蛋糕', 'https://commons.wikimedia.org/wiki/Special:FilePath/Shortcake_slice.jpg?width=900', 168.00, 1, 168.00),
(1, 4, '芒果慕斯', 'https://commons.wikimedia.org/wiki/Special:FilePath/Mango_mousse_cake_%288201823494%29.jpg?width=900', 158.00, 1, 158.00),
(1, 6, '抹茶慕斯', 'https://commons.wikimedia.org/wiki/Special:FilePath/Making_matcha_sponge_cake_at_home_04.jpg?width=900', 30.00, 1, 30.00),
(2, 5, '提拉米苏', 'https://commons.wikimedia.org/wiki/Special:FilePath/Inside_of_a_tiramisu_cake.jpg?width=900', 188.00, 1, 188.00),
(3, 11, '榴莲千层', 'https://commons.wikimedia.org/wiki/Special:FilePath/Durian_cake.jpg?width=900', 268.00, 1, 268.00);

INSERT INTO cart_items (user_id, cake_id, quantity) VALUES
(2, 7, 1),
(2, 12, 2),
(3, 14, 1);

