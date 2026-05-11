package com.smartcommunity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcommunity.entity.Worker;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WorkerMapper extends BaseMapper<Worker> {
    Long selectAvailableUserWorkerByCategory(@Param("category") String category);
}
