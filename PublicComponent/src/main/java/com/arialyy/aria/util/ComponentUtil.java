/*
 * Copyright (C) 2016 AriaLyy(https://github.com/AriaLyy/Aria)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.arialyy.aria.util;

import android.os.Handler;
import com.arialyy.aria.core.TaskOptionParams;
import com.arialyy.aria.core.inf.IEventHandler;
import com.arialyy.aria.core.inf.ITaskOption;
import com.arialyy.aria.core.inf.IUtil;
import com.arialyy.aria.core.listener.IEventListener;
import com.arialyy.aria.core.task.AbsTask;
import com.arialyy.aria.core.wrapper.AbsTaskWrapper;
import com.arialyy.aria.core.wrapper.ITaskWrapper;
import java.lang.ref.SoftReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * 组件工具，用于跨组件创建对应的工具类
 *
 * @Author lyy
 * @Date 2019-09-23
 */
public class ComponentUtil {
  public static final int COMPONENT_TYPE_HTTP = 1;
  public static final int COMPONENT_TYPE_FTP = 2;
  public static final int COMPONENT_TYPE_M3U8 = 3;

  private String TAG = CommonUtil.getClassName(getClass());
  private static volatile ComponentUtil INSTANCE = null;

  private ComponentUtil() {

  }

  public static ComponentUtil getInstance() {
    if (INSTANCE == null) {
      synchronized (ComponentUtil.class) {
        if (INSTANCE == null) {
          INSTANCE = new ComponentUtil();
        }
      }
    }

    return INSTANCE;
  }

  /**
   * 检查组件是否存在，不存在抛出异常退出
   *
   * @param componentType 组件类型{@link #COMPONENT_TYPE_FTP}, {@link #COMPONENT_TYPE_M3U8}, {@link
   * #COMPONENT_TYPE_HTTP}
   * @return true 组件存在
   */
  public boolean checkComponentExist(int componentType) {
    String errorStr = "", className = null;
    switch (componentType) {
      case COMPONENT_TYPE_M3U8:
        className = "com.arialyy.aria.m3u8.M3U8TaskOption";
        errorStr = "m3u8插件不存在，请添加m3u8插件";
        break;
      case COMPONENT_TYPE_FTP:
        className = "com.arialyy.aria.ftp.FtpTaskOption";
        errorStr = "ftp插件不存在，请添加ftp插件";
        break;
      case COMPONENT_TYPE_HTTP:
        className = "com.arialyy.aria.http.HttpTaskOption";
        errorStr = "http插件不存在，请添加http插件";
        break;
    }

    try {
      getClass().getClassLoader().loadClass(className);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      throw new IllegalArgumentException(errorStr);
    }
    return true;
  }

  /**
   * 创建下载工具
   *
   * @return 返回下载工具，创建失败返回null
   */
  public <T extends IUtil> T buildUtil(AbsTaskWrapper wrapper, IEventListener listener) {
    int requestType = wrapper.getRequestType();
    String className = null;
    switch (requestType) {
      case ITaskWrapper.M3U8_LIVE:
        className = "com.arialyy.aria.m3u8.live.M3U8LiveUtil";
        break;
      case ITaskWrapper.M3U8_VOD:
        className = "com.arialyy.aria.m3u8.vod.M3U8VodUtil";
        break;
      case ITaskWrapper.D_FTP:
        className = "com.arialyy.aria.ftp.download.FtpDLoaderUtil";
        break;
      case ITaskWrapper.D_HTTP:
        className = "com.arialyy.aria.http.download.HttpDLoaderUtil";
        break;
      case ITaskWrapper.U_FTP:
        className = "com.arialyy.aria.ftp.upload.FtpULoaderUtil";
        break;
      case ITaskWrapper.U_HTTP:
        className = "com.arialyy.aria.http.upload.HttpULoaderUtil";
        break;
      case ITaskWrapper.D_FTP_DIR:
        className = "com.arialyy.aria.ftp.download.FtpDirDLoaderUtil";
        break;
      case ITaskWrapper.DG_HTTP:
        className = "com.arialyy.aria.http.download.DGroupLoaderUtil";
        break;
    }
    if (className == null) {
      return null;
    }
    T util = null;
    try {
      Class<T> clazz = (Class<T>) getClass().getClassLoader().loadClass(className);
      Class[] paramTypes = { AbsTaskWrapper.class, IEventListener.class };
      Object[] params = { wrapper, listener };
      Constructor<T> con = clazz.getConstructor(paramTypes);
      util = con.newInstance(params);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
    return util;
  }

  /**
   * 创建任务事件监听
   *
   * @param wrapperType 任务类型{@link ITaskWrapper}
   * @return 返回事件监听，如果创建失败返回null
   */
  public <T extends IEventListener> T buildListener(int wrapperType, AbsTask task,
      Handler outHandler) {
    String className = null, errorStr = "请添加FTP插件";
    switch (wrapperType) {
      case ITaskWrapper.M3U8_LIVE:
      case ITaskWrapper.M3U8_VOD:
        className = "com.arialyy.aria.m3u8.M3U8Listener";
        errorStr = "请添加m3u8插件";
        break;
      case ITaskWrapper.D_FTP:
      case ITaskWrapper.D_HTTP:
        className = "com.arialyy.aria.core.listener.BaseDListener";
        break;
      case ITaskWrapper.U_FTP:
      case ITaskWrapper.U_HTTP:
        className = "com.arialyy.aria.core.listener.BaseUListener";
        break;
      case ITaskWrapper.DG_HTTP:
      case ITaskWrapper.D_FTP_DIR:
        className = "com.arialyy.aria.core.listener.DownloadGroupListener";
        break;
    }
    if (className == null) {
      return null;
    }
    T listener = null;
    try {
      Class<T> clazz = (Class<T>) getClass().getClassLoader().loadClass(className);
      Class[] paramTypes = { AbsTask.class, Handler.class };
      Object[] params = { task, outHandler };
      Constructor<T> con = clazz.getConstructor(paramTypes);
      listener = con.newInstance(params);
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException(errorStr);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
    return listener;
  }

  /**
   * 构建TaskOption信息
   *
   * @param clazz 实现{@link ITaskOption}接口的配置信息类
   * @param params 任务配置信息参数
   * @return 构建失败，返回null
   */
  public <T extends ITaskOption> T buildTaskOption(Class<T> clazz, TaskOptionParams params) {
    List<Field> fields = CommonUtil.getAllFields(clazz);
    T taskOption = null;
    try {
      taskOption = clazz.newInstance();
      for (Field field : fields) {
        field.setAccessible(true);
        Class type = field.getType();
        String key = field.getName();
        if (type != SoftReference.class) {
          Object obj = params.getParams().get(key);
          if (obj == null) {
            continue;
          }
          field.set(taskOption, obj);
        } else {
          IEventHandler handler = params.getHandler().get(key);
          if (handler == null) {
            continue;
          }
          field.set(taskOption, new SoftReference<>(handler));
        }
      }
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (InstantiationException e) {
      e.printStackTrace();
    }
    return taskOption;
  }
}
