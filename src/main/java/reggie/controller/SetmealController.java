package reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reggie.common.R;
import reggie.dto.DishDto;
import reggie.dto.SetmealDto;
import reggie.entity.Category;
import reggie.entity.Dish;
import reggie.entity.Setmeal;
import reggie.service.CategoryService;
import reggie.service.SetmealDishService;
import reggie.service.SetmealService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private CategoryService categoryService;

    /**
     * 新增套餐
     *
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        log.info(setmealDto.toString());

        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功");
    }

    /**
     * 套餐信息分页查询
     *
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        log.info("page = {}, pageSize = {}, name = {}", page, pageSize, name);

        //构造分页构造器
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        //setmealService查询出的结果只有套餐, 没有套餐分类, 使用SetmealDto可以查询到categoryName
        Page<SetmealDto> setmealDtoPage = new Page<>();

        //构造条件构造器: 过滤条件，排序条件
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name), Setmeal::getName, name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        //查询所有菜品
        setmealService.page(pageInfo, queryWrapper);

        //将查询结果存入setmealDtoPage中，除了records
        //records中存入的就是setmealService中查询出来的所有结果，类型是list
        BeanUtils.copyProperties(pageInfo, setmealDtoPage, "records");

        //单独提取records
        List<Setmeal> records = pageInfo.getRecords();

        //遍历records，使用records中的categoryId到categoryService中查询相应category，并将categoryName放到dishDto中
        List<SetmealDto> list = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();   //setmealDtoPage需要传入setmealDto对象
            BeanUtils.copyProperties(item, setmealDto); //setmealDto是空的，需要将刚才查询出来的item放进去

            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);

            if (category != null) {
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;

        }).collect(Collectors.toList());

        setmealDtoPage.setRecords(list);

        return R.success(setmealDtoPage);
    }
}
