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
dataGiornata[1] = "august 31 2008" 
dataGiornata[2] = "september 14 2008" 
dataGiornata[3] = "september 21 2008" 
dataGiornata[4] = "september 24 2008" 
dataGiornata[5] = "september 28 2008" 
dataGiornata[6] = "october 05 2008" 
dataGiornata[7] = "october 19 2008" 
dataGiornata[8] = "october 26 2008" 
dataGiornata[9] = "october 29 2008" 
dataGiornata[10] = "november 02 2008" 
dataGiornata[11] = "november 09 2008" 
dataGiornata[12] = "november 16 2008" 
dataGiornata[13] = "november 23 2008" 
dataGiornata[14] = "november 30 2008" 
dataGiornata[15] = "december 07 2008" 
dataGiornata[16] = "december 14 2008" 
dataGiornata[17] = "december 21 2008" 
dataGiornata[18] = "january 11 2009" 
dataGiornata[19] = "january 18 2009" 
dataGiornata[20] = "january 25 2009" 
dataGiornata[21] = "january 28 2009" 
dataGiornata[22] = "february 01 2009" 
dataGiornata[23] = "february 08 2009" 
dataGiornata[24] = "february 15 2009" 
dataGiornata[25] = "february 22 2009" 
dataGiornata[26] = "march 01 2009" 
dataGiornata[27] = "march 08 2009" 
dataGiornata[28] = "march 15 2009" 
dataGiornata[29] = "march 22 2009" 
dataGiornata[30] = "april 05 2009" 
dataGiornata[31] = "april 11 2009" 
dataGiornata[32] = "april 19 2009" 
dataGiornata[33] = "april 26 2009" 
dataGiornata[34] = "may 03 2009" 
dataGiornata[35] = "may 10 2009" 
dataGiornata[36] = "may 17 2009" 
dataGiornata[37] = "may 24 2009" 
dataGiornata[38] = "may 31 2009" 

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