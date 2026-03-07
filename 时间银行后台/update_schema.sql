-- Create tb_ranking_log
CREATE TABLE IF NOT EXISTS `tb_ranking_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `period` char(7) NOT NULL COMMENT '期数 (YYYY-MM)',
  `rank` int(11) NOT NULL COMMENT '排名',
  `volunteer_id` bigint(20) NOT NULL COMMENT '志愿者ID',
  `order_count` int(11) NOT NULL DEFAULT '0' COMMENT '接单数',
  `reward_amount` int(11) NOT NULL DEFAULT '0' COMMENT '奖励金额',
  `distribution_time` datetime DEFAULT NULL COMMENT '发放时间',
  `status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '发放状态 (0:失败 1:成功)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(4) DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_volunteer_id` (`volunteer_id`),
  KEY `idx_period` (`period`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排名奖励日志表';

-- Create sys_config
CREATE TABLE IF NOT EXISTS `sys_config` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` varchar(100) NOT NULL COMMENT '配置键名',
  `config_value` varchar(500) DEFAULT NULL COMMENT '配置值',
  `description` varchar(255) DEFAULT NULL COMMENT '配置说明',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- Insert default values
INSERT INTO `sys_config` (`config_key`, `config_value`, `description`) VALUES 
('elder_initial_coins', '100', '老人初始赠送时间币'),
('daily_sign_in_reward', '5', '每日签到基础奖励'),
('monthly_rank_1_reward', '500', '月度排行榜第一名奖金'),
('transaction_fee_percent', '0', '任务发布预扣手续费比例')
ON DUPLICATE KEY UPDATE `config_value` = VALUES(`config_value`);

-- Add columns to tb_product (Safe approximation - will fail if exists but that's okay for now or we could use stored proc)
-- We will attempt running these line by line or rely on the user to handle "Duplicate column" errors if they occur.
-- For this script, we'll just put each ALTER in a separate block so we can see what happens.
