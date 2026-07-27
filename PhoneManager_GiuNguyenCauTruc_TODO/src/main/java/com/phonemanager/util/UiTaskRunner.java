package com.phonemanager.util;

import javax.swing.SwingWorker;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * Chạy tác vụ I/O ở luồng nền và đưa kết quả trở lại Event Dispatch Thread.
 * Lớp generic này giúp các màn hình Swing không bị đơ khi truy vấn SQL Server.
 */
public final class UiTaskRunner {
    private UiTaskRunner() {
    }

    public static <T> void run(
            Callable<T> backgroundTask,
            Consumer<T> onSuccess,
            Consumer<Throwable> onError) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return backgroundTask.call();
            }

            @Override
            protected void done() {
                try {
                    onSuccess.accept(get());
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    onError.accept(exception);
                } catch (ExecutionException exception) {
                    Throwable cause = exception.getCause();
                    onError.accept(cause == null ? exception : cause);
                } catch (RuntimeException exception) {
                    onError.accept(exception);
                }
            }
        }.execute();
    }
}
