package com.xpcf.mall.member.dao;

import com.xpcf.mall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author xpcf
 * @email xpcf@gmail.com
 * @date 2020-12-12 03:32:23
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
