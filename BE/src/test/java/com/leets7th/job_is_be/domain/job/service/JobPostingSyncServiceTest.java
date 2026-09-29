package com.leets7th.job_is_be.domain.job.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobPostingSyncServiceTest {

    @Mock
    private JobSyncPageProcessor pageProcessor;
    @Mock
    private SyncExecutionStore executionStore;

    private JobPostingSyncService syncService;

    @BeforeEach
    void setUp() {
        syncService = new JobPostingSyncService(pageProcessor, executionStore);
    }

    @Test
    void sync는_페이지가_없을_때까지_processPage를_반복_호출한다() {
        when(pageProcessor.processPage(0)).thenReturn(true);
        when(pageProcessor.processPage(1)).thenReturn(true);
        when(pageProcessor.processPage(2)).thenReturn(false);

        syncService.sync();

        verify(pageProcessor).processPage(0);
        verify(pageProcessor).processPage(1);
        verify(pageProcessor).processPage(2);
    }

    @Test
    void syncOnStartup은_sync를_호출한다() {
        when(pageProcessor.processPage(0)).thenReturn(false);

        syncService.syncOnStartup();

        verify(pageProcessor).processPage(0);
    }
}
