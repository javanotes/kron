package org.reactiveminds.kron.dto;

public enum Command {

	SCHEDULE {
		@Override
		public String getCommand() {
			return "SCHEDULED";
		}

		@Override
		public int getPriority() {
			return 0;
		}
	},
	EXECUTE {
		@Override
		public String getCommand() {
			return "EXECUTABLE";
		}

		@Override
		public int getPriority() {
			return 0;
		}
	},
	SYSTEMSTAT {
		@Override
		public String getCommand() {
			return "SYSTEMSTAT";
		}

		@Override
		public int getPriority() {
			return 0;
		}
	},
	UNDEF {
		@Override
		public String getCommand() {
			return "";
		}

		@Override
		public int getPriority() {
			return 0;
		}
	};
	
	public abstract String getCommand();
	public abstract int getPriority();
	
}
