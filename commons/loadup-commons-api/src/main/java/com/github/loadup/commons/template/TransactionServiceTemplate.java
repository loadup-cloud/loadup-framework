// package com.github.loadup.commons.template;
//
/// *-
// * #%L
// * loadup-commons-dto
// * %%
// * Copyright (C) 2022 - 2024 loadup_cloud
// * %%
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as
// * published by the Free Software Foundation, either version 3 of the
// * License, or (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public
// * License along with this program.  If not, see
// * <http://www.gnu.org/licenses/gpl-3.0.html>.
// * #L%
// */
//
// import com.github.loadup.commons.error.AssertUtil;
// import com.github.loadup.commons.error.CommonException;
// import com.github.loadup.commons.result.*;
// import jakarta.annotation.PostConstruct;
// import java.util.Objects;
// import java.util.function.*;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Component;
// import org.springframework.transaction.support.TransactionTemplate;
//
// @Slf4j(topic = "SERVICE-LOGGER")
// @Component
// public final class TransactionServiceTemplate {
//  private static final TransactionServiceTemplate INSTANCE = new TransactionServiceTemplate();
//
//  public static TransactionServiceTemplate getInstance() {
//    return INSTANCE;
//  }
//
//  @Autowired private TransactionTemplate transactionTemplate;
//
//  @PostConstruct
//  public void init() {
//    getInstance().transactionTemplate = this.transactionTemplate;
//  }
//
//  private TransactionServiceTemplate() {}
//
//  public static <T extends Response> T execute(
//      Consumer<Void> checkParameter, // checkParameter
//      Supplier<T> process, // process
//      Function<Exception, Result> composeExceptionResponse, // 修改为 Function<Throwable, T>
//      Consumer<Void> composeDigestLog // composeDigestLog
//      ) {
//    T response = null;
//    try {
//      response =
//          getInstance()
//              .getTransactionTemplate()
//              .execute(
//                  status -> {
//                    T innerResponse;
//                    checkParameter.accept(null); // 执行参数检查
//                    innerResponse = process.get(); // 执行业务逻辑
//                    AssertUtil.notNull(innerResponse);
//                    innerResponse.setResult(Result.buildSuccess());
//                    return innerResponse;
//                  });
//    } catch (CommonException exception) {
//      log.error("service process, exception occurred:", exception.getMessage());
//      response.setResult(Result.buildFailure(exception.getResultCode()));
//    } catch (Exception throwable) {
//      log.error("service process,  exception occurred:", throwable.getMessage());
//      Result result = composeExceptionResponse.apply(throwable); // 异常处理
//      if (Objects.isNull(result)) {
//        result = Result.buildFailure(CommonResultCodeEnum.UNKNOWN);
//      }
//      response.setResult(result);
//    } catch (Throwable throwable) {
//      log.error("service process, unknown exception occurred:", throwable.getMessage());
//      response.setResult(Result.buildFailure(CommonResultCodeEnum.UNKNOWN));
//    } finally {
//      composeDigestLog.accept(null); // 执行日志
//    }
//    return response;
//  }
//
//  public TransactionTemplate getTransactionTemplate() {
//    return transactionTemplate;
//  }
//
//  public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
//    this.transactionTemplate = transactionTemplate;
//  }
// }
