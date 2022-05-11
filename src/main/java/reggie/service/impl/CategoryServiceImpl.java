package reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reggie.common.CustomExcetion;
import reggie.entity.Category;
import reggie.entity.Dish;
import reggie.entity.Setmeal;
import reggie.mapper.CategoryMapper;
import reggie.service.CategoryService;
import reggie.service.DishService;
import reggie.service.SetmealService;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    /**
     * 根据id查询分类, 删除前需要进行判断
     *
     * @param ids
     */
    @Override
    public void remove(Long ids) {
        //构造查询条件
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();

        //添加查询条件, 根据分类id进行查询
        dishLambdaQueryWrapper.eq(Dish::getCategoryId, ids);
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, ids);

        int dishCont = dishService.count(dishLambdaQueryWrapper);
        int setmealCont = setmealService.count(setmealLambdaQueryWrapper);

        //查询当前分类是否关联了菜品, 如果已经关联, 抛出一个业务异常
        if (dishCont > 0) {
            throw new CustomExcetion("当前分类下关联了菜品, 不能删除");
        }

        //查询当前分类是否关联了套餐, 如果已经关联, 抛出一个业务异常
        if (setmealCont > 0) {
            throw new CustomExcetion("当前分类下关联了套餐, 不能删除");
        }

        //都没有则删除分类
        super.removeById(ids);

    }
}
