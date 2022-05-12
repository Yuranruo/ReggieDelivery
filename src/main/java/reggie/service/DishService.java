package reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import reggie.dto.DishDto;
import reggie.entity.Dish;

public interface DishService extends IService<Dish> {

    //新增菜品, 同时保存菜品对应的口味数据, 需要操作两张表: dish, dish_flaver
    public void saveWithFlaver(DishDto dishDto);

    //根据id查询菜品信息及其口味信息
    public DishDto getByIdWithFlavor(Long id);

    public void updateWithFlaver(DishDto dishDto);

}
