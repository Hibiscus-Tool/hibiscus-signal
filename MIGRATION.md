# Migration Guide

## 项目重构说明

项目已从单模块结构重构为多模块 Maven 项目。

## 模块结构

### hibiscus-signal-parent
父 POM，管理所有子模块的版本和依赖。

### hibiscus-signal-core
核心模块，包含不依赖 Spring 的核心功能：
- `io.github.signal.core` - 核心信号处理类
- `io.github.signal.exception` - 异常处理
- `io.github.signal.utils` - 工具类

### hibiscus-signal-spring
Spring Boot 集成模块，包含 Spring 相关功能：
- `io.github.signal.spring` - Spring 注解和配置
- `io.github.signal.core.persistent` - 持久化支持（依赖 Spring）
- `io.github.signal.core.tractional` - 事务支持（依赖 Spring）
- `io.github.signal.core.eventsourcing` - 事件溯源（依赖 Spring）

### hibiscus-signal-examples
示例代码模块：
- `io.github.signal.core.eventsourcing.example` - 事件溯源示例

## 依赖变更

### 之前
```xml
<dependency>
    <groupId>io.github.heathcetide</groupId>
    <artifactId>cetide.hibiscus.signal</artifactId>
    <version>1.1.0</version>
</dependency>
```

### Spring Boot 应用（推荐）
```xml
<dependency>
    <groupId>io.github.heathcetide</groupId>
    <artifactId>hibiscus-signal-spring</artifactId>
    <version>1.1.0</version>
</dependency>
```

### 仅核心功能
```xml
<dependency>
    <groupId>io.github.heathcetide</groupId>
    <artifactId>hibiscus-signal-core</artifactId>
    <version>1.1.0</version>
</dependency>
```

## 构建

```bash
mvn clean install
```

这会构建所有模块。

## 注意事项

1. 旧的 `src` 目录已备份为 `src.old`
2. `spring.factories` 已更新，移除了 core 模块中的配置类
3. 包名保持不变，确保向后兼容

