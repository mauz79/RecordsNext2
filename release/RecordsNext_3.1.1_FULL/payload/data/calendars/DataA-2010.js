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
dataGiornata[1] = "august 29 2010" 
dataGiornata[2] = "september 12 2010" 
dataGiornata[3] = "september 19 2010" 
dataGiornata[4] = "september 22 2010" 
dataGiornata[5] = "september 26 2010" 
dataGiornata[6] = "october 03 2010" 
dataGiornata[7] = "october 17 2010" 
dataGiornata[8] = "october 24 2010" 
dataGiornata[9] = "october 31 2010" 
dataGiornata[10] = "november 07 2010" 
dataGiornata[11] = "november 10 2010" 
dataGiornata[12] = "november 14 2010" 
dataGiornata[13] = "november 21 2010" 
dataGiornata[14] = "november 28 2010" 
dataGiornata[15] = "december 05 2010" 
dataGiornata[16] = "december 12 2010" 
dataGiornata[17] = "december 19 2010" 
dataGiornata[18] = "january 06 2011" 
dataGiornata[19] = "january 09 2011" 
dataGiornata[20] = "january 16 2011" 
dataGiornata[21] = "january 23 2011" 
dataGiornata[22] = "january 30 2011" 
dataGiornata[23] = "february 02 2011" 
dataGiornata[24] = "february 06 2011" 
dataGiornata[25] = "february 13 2011" 
dataGiornata[26] = "february 20 2011" 
dataGiornata[27] = "february 27 2011" 
dataGiornata[28] = "march 06 2011" 
dataGiornata[29] = "march 13 2011" 
dataGiornata[30] = "march 20 2011" 
dataGiornata[31] = "april 03 2011" 
dataGiornata[32] = "april 10 2011" 
dataGiornata[33] = "april 17 2011" 
dataGiornata[34] = "april 24 2011" 
dataGiornata[35] = "may 01 2011" 
dataGiornata[36] = "may 08 2011" 
dataGiornata[37] = "may 15 2011" 
dataGiornata[38] = "may 22 2011" 

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