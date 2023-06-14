package tech.codingless.core.plugs.mybaties3.helper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import lombok.Data;
import tech.codingless.core.plugs.mybaties3.annotation.MyColumn;
import tech.codingless.core.plugs.mybaties3.util.MybatiesStringUtil;

public class ColumnHelper {
	private final static String REGEX = "^[a-zA-Z_0-9]+$";
	private static List<String> COLUMN_SKIP_UPDATE = List.of("id", "ver", "gmt_create", "gmt_write");
	private static List<String> COLUMN_SKIP_INSERT = List.of("gmt_write", "gmt_create", "ver");

	public static boolean isIncorrectColumn(String column) {
		return !isCorrectColumn(column);
	}

	public static boolean isCorrectColumn(String column) {
		if (MybatiesStringUtil.isEmpty(column)) {
			return false;
		}
		return column.matches(REGEX);
	}

	public static class ColumnService {

		private List<Column> columns = null;

		public ColumnService(List<Column> columns) {
			this.columns = columns;
		}

		public ColumnService each(Consumer<Column> action) {
			columns.forEach(column -> {
				action.accept(column);
			});
			return this;
		}

		public ColumnService eachInsertColumn(Consumer<Column> action) {
			columns.stream().filter(item -> !COLUMN_SKIP_INSERT.contains(item.getColumnName())).forEach(column -> {
				action.accept(column);
			});
			return this;
		}

		public ColumnService eachUpdateColumn(Consumer<Column> action) {
			columns.stream().filter(item -> !COLUMN_SKIP_UPDATE.contains(item.getColumnName())).forEach(column -> {
				action.accept(column);
			});
			return this;
		}
	}

	@Data
	public static class Column {
		private String columnName;
		private String attrName;
	}

	public static ColumnService parse(Class<?> clazz) {
		List<Column> columns = new ArrayList<>();
		for (Method method : clazz.getMethods()) {
			String methodName = method.getName();
			if (MyTableColumnParser.needSkipMethodName(methodName)) {
				continue;
			}
			String attrName = MyTableColumnParser.methodName2attrName(methodName);
			String columnName = null;
			try {
				Field filed = clazz.getDeclaredField(attrName);
				MyColumn myColumn = filed.getAnnotation(MyColumn.class);
				if (myColumn != null && myColumn.virtual()) {
					continue;
				}
				if (myColumn != null && MybatiesStringUtil.isNotEmpty(myColumn.name())) {
					columnName = myColumn.name();
				}
			} catch (Exception e1) {

			}
			if (MybatiesStringUtil.isEmpty(columnName)) {
				columnName = CommonSQLHelper.change2dbFormat(attrName);
			}

			Column column = new Column();
			column.setAttrName(attrName);
			column.setColumnName(columnName.toLowerCase());
			columns.add(column);
		}
		ColumnService columnService = new ColumnHelper.ColumnService(columns);
		return columnService;
	}

}
