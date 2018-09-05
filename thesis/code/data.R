
## Data pre-processing

# Specify dataset to load
dataset_id <- "consumer-00000004" 

# Function for easy string pasting
"%&%"<-function(x, y) paste(x, y, sep = "")

# Load packages
packages <- c("data.table", "tidyverse", "tidyquant", "cowplot", "tibbletime",
              "keras")

invisible(lapply(packages, library, character.only = TRUE))

# Load data
raw_data <- fread(dataset_id%&%".csv",
                  header    = T,
                  sep       = ',',
                  integer64 = "numeric")

raw_data$time <- as_datetime(raw_data$time/1000, tz = "CET")

# Format data
unscaled_data <- raw_data %>%
    mutate(cons = c(NA, diff(energy, lag = 1))) %>%
    select(time, cons) %>%
    filter(!is.na(cons)) %>%
    as_tbl_time(time)

# Split train and test data
idx_train <- 1:max_index_val
idx_test  <- min_index_test:max_index_test

train_data <- unscaled_data[idx_train, ]
test_data <- unscaled_data[idx_test, ]

# Scale data to 0/1-interval with mean and std from train_data
train_min <- min(log(train_data$cons))
train_max <- max(log(train_data$cons))

scaled_data <- unscaled_data %>%
    mutate(cons = (log(cons)-train_min)/(train_max-train_min))

# 15-min aggregation
aggr_data <- scaled_data %>%
    mutate(time_aggr = collapse_index(index = time,
                                      period = "15 minutely",
                                      side = "end",
                                      start_date = as_datetime(min(time), tz = "CET"))) %>%
    group_by(time_aggr) %>%
    summarize(cons_aggr = sum(cons))

# Drop time stamps
data <- scaled_data %>%
    select(cons) %>%
    as.matrix()

targets <- aggr_data %>%
    select(cons_aggr) %>%
    as.matrix()
