package io.github.signal.core.persistent;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileSignalPersistence {

    private static final Logger log = LoggerFactory.getLogger(FileSignalPersistence.class);

    // 对象映射器
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 保存数据到文件，采用增量保存
    public static void saveToFileIncrementally(Object data, String filePath) {
        File file = new File(filePath);

        try {
            // 如果文件不存在，创建文件
            if (!file.exists()) {
                file.createNewFile();
            }

            // 读取现有数据
            List<Object> existingData = loadFromFile(filePath);

            // 如果现有数据为空，则初始化为一个空的列表
            if (existingData == null) {
                existingData = new ArrayList<>();
            }

            // 将新的数据添加到现有数据
            existingData.add(data);

            // 将更新后的数据写回文件
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, existingData);
        } catch (IOException e) {
            log.error("保存数据到文件失败：{}", e.getMessage());
            e.printStackTrace();
        }
    }

    // 从文件中加载数据，添加空文件和格式错误的处理
    public static <T> List<T> loadFromFile(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists() && file.length() > 0) {
                // 读取文件中的数据
                return objectMapper.readValue(file, new TypeReference<List<T>>() {});
            } else {
                log.warn("文件为空或不存在，返回空列表");
            }
        } catch (IOException e) {
            log.error("从文件中加载数据失败：{}", e.getMessage());
            // 如果发生读取错误，返回一个空的列表
            return new ArrayList<>();
        }
        return new ArrayList<>();
    }
}
