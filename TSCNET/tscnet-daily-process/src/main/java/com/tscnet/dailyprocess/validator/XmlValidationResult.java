package com.tscnet.dailyprocess.validator;

import java.util.List;

public record XmlValidationResult(boolean valid, List<String> errors ) { }
