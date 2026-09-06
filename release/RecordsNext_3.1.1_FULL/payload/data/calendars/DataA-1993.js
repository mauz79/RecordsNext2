var dataGiornata = new Array()
// Immettere le date nel formato inglese: Mese Giorno Anno
// Gennaio = January
// Febbraio = February
// Marzo = March
// Maprile = April
// Maggio = May
// Giugno = June
// Luglio = July
// Agosto = August
// Settembre = September
// Ottobre = October
// Novembre = November
// Dicembre = December
dataGiornata[1] = "august 29 1993" 
dataGiornata[2] = "september 05 1993" 
dataGiornata[3] = "september 08 1993" 
dataGiornata[4] = "september 12 1993" 
dataGiornata[5] = "september 19 1993" 
dataGiornata[6] = "september 26 1993" 
dataGiornata[7] = "october 03 1993" 
dataGiornata[8] = "october 17 1993" 
dataGiornata[9] = "october 24 1993" 
dataGiornata[10] = "october 31 1993" 
dataGiornata[11] = "november 07 1993" 
dataGiornata[12] = "november 21 1993" 
dataGiornata[13] = "november 28 1993" 
dataGiornata[14] = "december 05 1993" 
dataGiornata[15] = "december 12 1993" 
dataGiornata[16] = "december 19 1993" 
dataGiornata[17] = "january 02 1994" 
dataGiornata[18] = "january 09 1994" 
dataGiornata[19] = "january 16 1994" 
dataGiornata[20] = "january 23 1994" 
dataGiornata[21] = "january 30 1994" 
dataGiornata[22] = "february 06 1994" 
dataGiornata[23] = "february 13 1994" 
dataGiornata[24] = "february 20 1994" 
dataGiornata[25] = "february 27 1994" 
dataGiornata[26] = "march 06 1994" 
dataGiornata[27] = "march 13 1994" 
dataGiornata[28] = "march 20 1994" 
dataGiornata[29] = "march 27 1994" 
dataGiornata[30] = "april 02 1994" 
dataGiornata[31] = "april 10 1994" 
dataGiornata[32] = "april 17 1994" 
dataGiornata[33] = "april 24 1994" 
dataGiornata[34] = "may 01 1994" 


function initArray() {  
	this.length = initArray.arguments.length
    for (var i = 0; i < this.length; i++)
    this[i+1] = initArray.arguments[i]
}
var DOWArray = new initArray("Dom","Lun","Mar","Mer","Gio","Ven","Sab")
var MOYArray = new initArray("Gen","Feb","Mar","Apr","Mag","Giu","Lug","Ago","Set","Ott","Nov","Dic")
var Year
//for (t = 1; t < dataGiornata.length-1 ;t++ ) {
for (t = 1; t < dataGiornata.length ;t++ ) {
	data = new Date(dataGiornata[t])
	Year = data.getYear()
	if (Year < 2000)
		Year = Year + 1900
	dataGiornata[t] = DOWArray[(data.getDay()+1)] + " " + data.getDate() + " " + MOYArray[(data.getMonth()+1)] + " " + Year
}