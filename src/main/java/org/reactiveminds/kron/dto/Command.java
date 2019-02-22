package org.reactiveminds.kron.dto;

public enum Command {

	NEWJOB {
		@Override
		public String getCommand() {
			return "NEWJOB";
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
