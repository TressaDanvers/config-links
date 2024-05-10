fun main(vararg args: String) {
  if (args.isEmpty()) readAndExecuteOptionsFromArguments("-h")
	else readAndExecuteOptionsFromArguments(*args)
}