package com.music.setting.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.music.setting.entity.UserSetting;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserSettingMapper extends BaseMapper<UserSetting> {
}
