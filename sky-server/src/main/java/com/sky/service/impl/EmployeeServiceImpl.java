package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        // 1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        // 2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            // 账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 密码比对
        // 使用Spring框架提供的工具类进行，密码md5加密
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            // 密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            // 账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        // 3、返回实体对象
        return employee;
    }

    /**
     * 新增员工
     *
     * @param employeeDTO
     */
    @Override
    public void save(EmployeeDTO employeeDTO) {
        // 创建employee对象
        Employee employee = new Employee();

        // 拷贝对象信息
        BeanUtils.copyProperties(employeeDTO, employee);

        // 手动设置未包含的信息
        // 设置密码,初始默认密码是123456
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));

        // 设置用户状态,初始状态为1(正常状态是1,锁定状态是0)
        employee.setStatus(StatusConstant.ENABLE);

        // 设置创建时间(当前时间)
        // employee.setCreateTime(LocalDateTime.now());
        // 设置更新时间(当前时间)
        // employee.setUpdateTime(LocalDateTime.now());

        // 从当前线程中获取用户id
        // Long id = BaseContext.getCurrentId();
        // 设置当前记录创建人id
        // employee.setCreateUser(id);
        // 设置当前记录修改人id
        // employee.setUpdateUser(id);

        // 调用mapper
        employeeMapper.insert(employee);
    }

    /**
     * 员工分页查询
     *
     * @param employeePageQueryDTO
     * @return
     */
    @Override
    public PageResult page(EmployeePageQueryDTO employeePageQueryDTO) {
        // select * from employee limit (page, pageSize) where name like %?%
        // 基于插件进行分页查询(基于PageHelper)
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());

        // 调用mapper
        Page<Employee> page = employeeMapper.page(employeePageQueryDTO);
        // 获取总记录数
        long total = page.getTotal();
        // 获取记录集合
        List<Employee> records = page.getResult();

        return new PageResult(total, records);
    }

    /**
     * 员工状态启用与禁用
     *
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        // 创建对象并设置属性值
        /*Employee employee = new Employee();
        employee.setStatus(status);
        employee.setId(id);*/

        Employee employee = Employee.builder()
                .status(status)
                .id(id)
                .build();
        employeeMapper.updateById(employee);
    }

    /**
     * 根据id查询员工信息
     *
     * @param id
     * @return
     */
    @Override
    public Employee getById(Long id) {
        // 根据id查询数据库获取Employee对象
        Employee employee = employeeMapper.getById(id);
        // 判断对象是否存在
        if (employee == null || employee.toString().equals(" ")) {
            // 对象不存在,返回错误信息
            throw new AccountNotFoundException("该用户不存在");
        }
        // 将密码隐藏
        employee.setPassword("******");
        // 对象存在,返回对象给前端
        return employee;
    }

    /**
     * 编辑员工信息
     *
     * @param employeeDTO
     */
    @Override
    public void update(EmployeeDTO employeeDTO) {
        // 创建employee对象
        Employee employee = new Employee();
        // 拷贝DTO对象信息给employee
        BeanUtils.copyProperties(employeeDTO, employee);
        // 设置通用update方法中的属性
        // employee.setUpdateTime(LocalDateTime.now());
        // employee.setUpdateUser(BaseContext.getCurrentId());
        // 调用mapper执行修改
        employeeMapper.updateById(employee);

    }

}
