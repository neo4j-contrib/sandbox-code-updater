package com.neo4j.sandbox.updater;

import java.util.Objects;

public class Metadata {

    private final String query;

    private final String parameterName;

    private final String parameterValue;

    private final String resultColumn;

    private final String expectedResult;

    public Metadata(String query, String parameterName, String parameterValue, String resultColumn, String expectedResult) {
        this.query = query;
        this.parameterName = parameterName;
        this.parameterValue = parameterValue;
        this.resultColumn = resultColumn;
        this.expectedResult = expectedResult;
    }

    public String getQuery() {
        return query;
    }

    public String getParameterName() {
        return parameterName;
    }

    public String getParameterValue() {
        return parameterValue;
    }

    public String getResultColumn() {
        return resultColumn;
    }

    public String getExpectedResult() {
        return expectedResult;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Metadata that = (Metadata) o;
        return Objects.equals(query, that.query) &&
                Objects.equals(parameterName, that.parameterName) &&
                Objects.equals(parameterValue, that.parameterValue) &&
                Objects.equals(resultColumn, that.resultColumn) &&
                Objects.equals(expectedResult, that.expectedResult);
    }

    @Override
    public int hashCode() {
        return Objects.hash(query, parameterName, parameterValue, resultColumn, expectedResult);
    }

    @Override
    public String toString() {
        return "Metadata{" +
                "query='" + query + '\'' +
                ", parameterName='" + parameterName + '\'' +
                ", parameterValue='" + parameterValue + '\'' +
                ", resultColumn='" + resultColumn + '\'' +
                ", expectedResult='" + expectedResult + '\'' +
                '}';
    }
}
