package com.tscnet.dailyprocess.dto;

import com.tscnet.dailyprocess.model.ExecutionStatus;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessFileLogDTO {

    private Long id;

    private String fileName;

    private String status;

    private String errorMessage;
}
