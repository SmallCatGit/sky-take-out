package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.BaseException;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
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

    @Autowired
    private SetmealDishMapper setmealDishMapper;

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

    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult page(DishPageQueryDTO dishPageQueryDTO) {
        // 基于pageHelper进行分页查询
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        // 调用mapper进行分页查询
        Page<DishVO> dishVOPage = dishMapper.page(dishPageQueryDTO);
        return new PageResult(dishVOPage.getTotal(), dishVOPage.getResult());
    }

    /**
     * 批量删除菜品
     *
     * @param ids
     */
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        // 查询菜品是否被套餐关联
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if (setmealIds != null && setmealIds.size() > 0) {
            // 菜品被套餐关联,不能删除
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        // 查询菜品表中的菜品是否在起售
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if (dish != null && dish.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            } else if (dish == null) {
                throw new BaseException("菜品已删除,请刷新页面重试!");
            }
        }
        // 删除菜品信息
        for (Long id : ids) {
            dishMapper.deleteById(id);
            // 删除菜品关联的口味数据
            dishFlavorMapper.deleteByDishId(id);
        }
    }

    /**
     * 根据id查询菜品和对应的口味数据
     *
     * @param id
     * @return
     */
    @Override
    public DishVO getByIdWithFlavor(Long id) {
        // 根据id查询菜品信息
        Dish dish = dishMapper.getById(id);

        // 根据菜品id查询菜品关联的口味信息
        List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);

        // 将查询到的数据封装到DishVO中
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(dishFlavors);

        return dishVO;
    }

    /**
     * 修改菜品和菜品对应的口味信息
     *
     * @param dishDTO
     */
    @Override
    public void updateWithFlavor(DishDTO dishDTO) {
        // 创建dish对象(dishDTO对象中含有其他属性,应该排除)
        Dish dish = new Dish();
        // 拷贝属性
        BeanUtils.copyProperties(dishDTO, dish);
        // 修改菜品基本信息
        dishMapper.update(dish);

        // 获取菜品的口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        // 删除原始的口味数据
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        // 判断是否有口味数据
        if (flavors != null && flavors.size() > 0) {
            // 口味可能有新增,新增的口味应该和菜品id对应,否则口味表中就会缺少菜品id字段,需要遍历设置口味表中的菜品id
            flavors.forEach(dishFlavor -> dishFlavor.setDishId(dishDTO.getId()));
            // 插入新的口味数据
            dishFlavorMapper.insertBatch(flavors);
        }
    }
}
