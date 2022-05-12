package reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reggie.common.CustomExcetion;
import reggie.dto.SetmealDto;
import reggie.entity.Setmeal;
import reggie.entity.SetmealDish;
import reggie.mapper.SetmealMapper;
import reggie.service.SetmealDishService;
import reggie.service.SetmealService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 新增套餐, 同时保存套餐和菜品的关联关系
     *
     * @param setmealDto
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐的基本信息, 操作setmeal, 执行insert操作
        this.save(setmealDto);

        //取出setmealDishes, 以存入setmealDishService中
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();

        //SetmealDishes中没有套餐id, 需要手动装入id
        setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        //保存套餐和菜品的关联信息, 操作setmeal_dish, 执行insert操作
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 删除套餐, 同时删除套餐和菜品的关联关系
     *
     * @param ids
     */
    @Override
    @Transactional
    public void deleteWithDish(List<Long> ids) {
        //select count(*) from setmeal where id in (1,2,3) and status = 1
        //查询套餐状态, 如果为启售状态则无法删除
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ids != null, Setmeal::getId, ids);
        queryWrapper.eq(Setmeal::getStatus, 1);

        //如果不能删除, 抛出一个业务异常
        int count = this.count(queryWrapper);
        if (count > 0) throw new CustomExcetion("套餐售卖中, 不可删除");

        //如果套餐为停售状态, 先删除套餐表中的数据-----setmeal
        this.removeByIds(ids);

        //delete from setmeal_dish where setmeal_id in (1,2,3)
        //由于setmealDishService中需要setmeal_id删除数据, 因此需要在setmealDish中使用ids查询setmeal_id
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDish::getSetmealId, ids);

        //删除关系表中的数据----setmeal_dish
        setmealDishService.remove(lambdaQueryWrapper);

    }

}
