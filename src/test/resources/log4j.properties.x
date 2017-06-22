# Default log level
log4j.rootCategory=INFO, console

log4j.appender.console=org.apache.log4j.ConsoleAppender
log4j.appender.console.layout=org.apache.log4j.PatternLayout
#log4j.appender.console.layout.ConversionPattern=%-5p %d [%t] %c: %m%n
log4j.appender.console.layout.ConversionPattern= %-5p %-30.30c{1}: %m%n

# TESTING

