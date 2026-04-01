ALTER TABLE `tb_product` ADD COLUMN `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '商品状态（1:上架 0:下架）';
ALTER TABLE `tb_product` ADD COLUMN `description` text COMMENT '商品描述';
ALTER TABLE `tb_product` ADD COLUMN `sales_count` int(11) NOT NULL DEFAULT 0 COMMENT '销量';
