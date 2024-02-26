package com.atguigu.spzx.manager.lisener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: smionf
 * @Date: 2024/02/26/18:25
 * @Description:
 */
public class ExcelListener<T> extends AnalysisEventListener<T> {

    List<T> datas = new ArrayList<>();

    @Override
    public void invoke(T data, AnalysisContext analysisContext) {
        System.out.println("ExcelListener - invoke执行了");
        datas.add(data);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        System.out.println("ExcelListener - doAfterAllAnalysed执行了");
    }

    public List<T> getDatas() {
        return datas;
    }
}
