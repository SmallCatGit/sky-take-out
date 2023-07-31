package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据菜品id查询对应的套餐id(多对多)
     * @param dishIds
     * @return
     */
    // select setmeal_id from setmeal_dish where dish_id in (ids)
    public List<Long> getSetmealIdsByDishIds(List<Long> dishIds);
}
