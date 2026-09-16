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
dataGiornata[1] = "september 04 1994" 
dataGiornata[2] = "september 11 1994" 
dataGiornata[3] = "september 18 1994" 
dataGiornata[4] = "september 25 1994" 
dataGiornata[5] = "october 02 1994" 
dataGiornata[6] = "october 16 1994" 
dataGiornata[7] = "october 23 1994" 
dataGiornata[8] = "october 30 1994" 
dataGiornata[9] = "november 06 1994" 
dataGiornata[10] = "november 20 1994" 
dataGiornata[11] = "november 27 1994" 
dataGiornata[12] = "december 04 1994" 
dataGiornata[13] = "december 11 1994" 
dataGiornata[14] = "december 18 1994" 
dataGiornata[15] = "january 08 1995" 
dataGiornata[16] = "january 15 1995" 
dataGiornata[17] = "january 22 1995" 
dataGiornata[18] = "january 29 1995" 
dataGiornata[19] = "february 05 1995" 
dataGiornata[20] = "february 12 1995" 
dataGiornata[21] = "february 19 1995" 
dataGiornata[22] = "february 26 1995" 
dataGiornata[23] = "march 05 1995" 
dataGiornata[24] = "march 12 1995" 
dataGiornata[25] = "march 19 1995" 
dataGiornata[26] = "april 02 1995" 
dataGiornata[27] = "april 09 1995" 
dataGiornata[28] = "april 15 1995" 
dataGiornata[29] = "april 23 1995" 
dataGiornata[30] = "april 30 1995" 
dataGiornata[31] = "may 07 1995" 
dataGiornata[32] = "may 14 1995" 
dataGiornata[33] = "may 21 1995" 
dataGiornata[34] = "may 28 1995" 


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