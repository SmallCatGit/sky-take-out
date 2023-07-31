package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    /**
     * 新增菜品和对应的口味数据
     *
     * @param dishDTO
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        // 创建菜品对象
        Dish dish = new Dish();
        // 菜品对象相关属性拷贝
        BeanUtils.copyProperties(dishDTO, dish);
        // 向菜品表中插入1条菜品数据
        dishMapper.insert(dish);

        // 获取insert语句生成的主键值（xml中已经配置好,可直接get）
        Long dishId = dish.getId();

        // 获取口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            // 遍历集合,为所有的dishId赋值
            flavors.forEach(dishFlavor -> dishFlavor.setDishId(dishId));

            // 向口味表中插入多条数据
            dishFlavorMapper.insertBatch(flavors);
        }

    }
}
