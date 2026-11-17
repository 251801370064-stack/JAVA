package com.library.model;

import java.util.List;

public record ReportData(List<String> columns, List<Object[]> rows) {
}
