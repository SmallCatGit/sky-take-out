package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增套餐和套餐中的菜品数据
     *
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDTO setmealDTO) {
        // 创建套餐对象
        Setmeal setmeal = new Setmeal();
        // 拷贝属性
        BeanUtils.copyProperties(setmealDTO, setmeal);
        // 保存套餐基础数据
        setmealMapper.insert(setmeal);

        // 获取套餐id
        Long setmealId = setmeal.getId();
        // 获取套餐中的菜品数据
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        // 判断菜品数据是否存在
        if (setmealDishes != null && setmealDishes.size() > 0) {
            // 将套餐id和每个菜品绑定
            setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(setmealId));
            // 保存套餐中的菜品数据
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    /**
     * 套餐分页查询
     *
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult page(SetmealPageQueryDTO setmealPageQueryDTO) {
        // 基于pageHelper分页查询
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());

        // 调用mapper执行分页查询
        Page<SetmealVO> setmealVOPage = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(setmealVOPage.getTotal(), setmealVOPage.getResult());
    }
}
