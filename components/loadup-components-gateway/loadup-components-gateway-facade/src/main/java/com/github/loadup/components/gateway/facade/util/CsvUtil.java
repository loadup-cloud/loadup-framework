package com.github.loadup.components.gateway.facade.util;

import com.opencsv.bean.*;
import com.opencsv.*;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import java.io.*;
import java.util.List;

public class CsvUtil {
    /**
     * csv文件导入并转为java对象
     *
     * @param fileName
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> List<T> readCSVFile(String fileName, Class<T> clazz) {
        try {
            FileReader reader = new FileReader(fileName);
            CSVReader csvReader = new CSVReader(reader);

            HeaderColumnNameMappingStrategy<T> strategy = new HeaderColumnNameMappingStrategy<>();
            strategy.setType(clazz);

            CsvToBean<T> csvToBean = new CsvToBeanBuilder<T>(csvReader)
                    .withType(clazz)
                    //将第一行视为标题行
                    .withMappingStrategy(strategy)
                    .build();

            return csvToBean.parse();

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将java对象转为csv文件导出
     *
     * @param fileName
     * @param list
     * @param <T>
     * @return
     */
    public static <T> void writeCSVFile(String fileName, List<T> list) {

        try {
            FileWriter writer = new FileWriter(fileName);

            StatefulBeanToCsv<T> beanToCsv = new StatefulBeanToCsvBuilder<T>(writer)
                    .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                    // 启用标题行
                    .withOrderedResults(true)
                    .build();

            // 添加标题行（将 Person 对象的字段名作为标题）
            writer.write("Name, Age, Sex\n");

            beanToCsv.write(list);
            writer.close();

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (CsvRequiredFieldEmptyException e) {
            throw new RuntimeException(e);
        } catch (CsvDataTypeMismatchException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
