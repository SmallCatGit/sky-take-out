package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
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

    @Autowired
    private DishMapper dishMapper;

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

    /**
     * 套餐起售与停售
     *
     * @param status
     * @param id
     */
    @Override
    public void status(Integer status, Long id) {
        // 判断套餐是否打算起售
        if (status == StatusConstant.ENABLE) {
            // 将要起售,通过套餐id获取套餐中的菜品
            List<Dish> dishes = dishMapper.getBySetmealId(id);
            // 判断菜品是否为空
            if (dishes != null && dishes.size() > 0) {
                // 不为空,判断是否有停售菜品
                dishes.forEach(dish -> {
                    if (dish.getStatus() == StatusConstant.DISABLE)
                        // 有停售菜品,提示套餐内包含未启售菜品，无法启售
                        throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                });
            }
        }

        // 没有停售菜品,根据套餐id修改套餐状态
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        setmealMapper.update(setmeal);
    }

    /**
     * 根据id查询套餐,用于修改套餐的页面回显
     *
     * @param id
     * @return
     */
    @Override
    public SetmealVO getById(Long id) {
        // 根据套餐id查询套餐表获取套餐对象
        Setmeal setmeal = setmealMapper.getById(id);

        // 根据套餐id获取套餐菜品表中的数据
        List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);

        // 创建SetmealVO对象
        SetmealVO setmealVO = new SetmealVO();
        // 拷贝套餐数据
        BeanUtils.copyProperties(setmeal, setmealVO);
        // 设置菜品数据
        setmealVO.setSetmealDishes(setmealDishes);

        return setmealVO;
    }

    /**
     * 修改套餐与套餐中的菜品
     *
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void update(SetmealDTO setmealDTO) {
        // 创建套餐对象
        Setmeal setmeal = new Setmeal();
        // 封装数据
        BeanUtils.copyProperties(setmealDTO, setmeal);
        // 修改套餐
        setmealMapper.update(setmeal);

        // 获取套餐id
        Long setmealId = setmeal.getId();

        // 获取套餐中的菜品数据
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        // 删除套餐中原始的菜品数据
        setmealDishMapper.deleteBySetmealId(setmealId);
        // 设置套餐菜品表中的套餐id(setmeal_id)
        setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(setmealId));
        // 插入数据到套餐菜品表中
        setmealDishMapper.insertBatch(setmealDishes);
    }

    /**
     * 批量删除套餐和套餐中的菜品
     *
     * @param ids
     */
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        // 判断id是否存在
        if (ids == null || ids.size() == 0) {
            // 不存在,返回错误信息
            throw new DeletionNotAllowedException(MessageConstant.NO_CHOOSE_SETMEAL);
        }
        // 根据id获取每一个套餐对象
        ids.forEach(id -> {
            Setmeal setmeal = setmealMapper.getById(id);
            // 判断套餐是否是起售状态
            if (setmeal.getStatus() == StatusConstant.ENABLE) {
                // 起售中的套餐不允许删除
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        });
        // 停售状态,根据套餐id删除套餐菜品表中的数据
        for (Long id : ids) {
            setmealDishMapper.deleteBySetmealId(id);
        }
        // 再根据id删除套餐表中的数据
        setmealMapper.deletedeleteBatch(ids);
    }
}
