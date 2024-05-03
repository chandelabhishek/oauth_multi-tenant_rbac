package com.oauth.example.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Calendar;

class DateUtilsTest {
    /**
     * Method under test: {@link DateUtils#getMonthYear(int)}
     */
    @Test
    void testGetMonthYear() {
        var calMock = Mockito.mock(Calendar.class);
        Mockito.when(calMock.get(Calendar.YEAR)).thenReturn(2024);
        Assertions.assertTrue("02/26".compareTo(DateUtils.getMonthYear(2)) <= 0);
    }
}
