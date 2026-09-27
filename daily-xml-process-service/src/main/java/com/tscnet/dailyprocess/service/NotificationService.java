package com.tscnet.dailyprocess.service;
public interface NotificationService { void notifyProcessSuccess(Long executionId); void notifyProcessFailure(Long executionId,String reason); }
