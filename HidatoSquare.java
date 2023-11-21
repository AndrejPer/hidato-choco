	public void solve() {
		// BEGINNING of MY CODE
		int n = 6;
		Model model = new Model(n + " Hidato problem");
		IntVar[][] vars = new IntVar[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				vars[i][j] = model.intVar("C_(" + i + ", " + j + ")", 1, n * n); // naming the vars
			}
		}
		
		// initializing arrays for traversing the neighbors of a cell
		int[] rowOffset = {-1, -1, -1, 0, 0, 1, 1, 1};
		int[] colOffset = {-1, 0, 1, -1, 1, -1, 0, 1};

		// constrain neighbors by cell value
		for(int i = 0; i < n; i++) {
			for(int j = 0; j < n; j++) {
				if(gridInit[i][j] > 0) model.arithm(vars[i][j], "=", gridInit[i][j]).post(); // fix the value of predetermined cells

				// arrays for neighbors' constraints
				Constraint[] succCstrs = new Constraint[0];
				Constraint[] predCstrs = new Constraint[0];

				//go through potential neighbors
				for(int k = 0; k < rowOffset.length; k++) {
					int nbhRow = i + rowOffset[k];
					int nbhCol = j + colOffset[k];
					if(nbhCol >= n || nbhCol < 0 || nbhRow >= n || nbhRow < 0) continue; // if out of bound, skip indices

					// if greater than 1, we define predecessor constraint
					if (gridInit[i][j] > 1) {
						int lenPre = predCstrs.length;

						Constraint[] tempP = new Constraint[lenPre + 1];
						System.arraycopy(predCstrs, 0, tempP, 0, lenPre);
						tempP[lenPre] = model.arithm(vars[nbhRow][nbhCol], "=", vars[i][j], "-", 1);

						// updating the pointer to the array of predecessor constraints to the new, extended array
						predCstrs = tempP;
					}

					// if less than 36, we define successor constraint 
					if (gridInit[i][j] < n * n) {
            // since the program works with a sqaure Hidato, it is easy to use n^2 as the bound
            // update this if solving a non-square Hidato
						int lenSuc = succCstrs.length;

						Constraint[] tempS = new Constraint[lenSuc + 1];
						System.arraycopy(succCstrs, 0, tempS, 0, lenSuc);
						tempS[lenSuc] = model.arithm(vars[nbhRow][nbhCol], "=", vars[i][j], "+", 1);
						// updating the pointer to the array of successor constraints to the new, extended array
						succCstrs = tempS;
					}
				}
				// doing logical OR on neighbors for successor and predecessor cells, if any
				if(succCstrs.length > 0) model.or(succCstrs).post();
				if(predCstrs.length > 0) model.or(predCstrs).post();
			}
		}

		IntVar[] vars_flat = org.chocosolver.util.tools.ArrayUtils.flatten(vars);
		model.allDifferent(vars_flat).post();

		Solution solution = model.getSolver().findSolution();

		// copying solutions to grid
		for(int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				grid[i][j] = vars[i][j].getValue();
			}
		}
	}
